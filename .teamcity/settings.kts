import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.PullRequests
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.merge
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.pullRequests
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2019_2.ideaInspections
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2020.1"

project {

    buildType(Build)

    features {
        feature {
            id = "PROJECT_EXT_2"
            type = "JetBrains.SharedResources"
            param("quota", "6")
            param("name", "TR1")
            param("type", "quoted")
        }
        feature {
            id = "PROJECT_EXT_4"
            type = "CloudImage"
            param("image-name-prefix", "AMI Golden Image 01")
            param("use-spot-instances", "false")
            param("security-group-ids", "sg-1cdaad75,")
            param("profileId", "amazon-1")
            param("ebs-optimized", "false")
            param("instance-type", "t2.micro")
            param("amazon-id", "TCTestImage_06")
            param("source-id", "AMI Golden Image 01")
        }
        feature {
            id = "amazon-1"
            type = "CloudProfile"
            param("profileServerUrl", "http://09c8aa8714af.ngrok.io")
            param("secure:access-id", "credentialsJSON:ed817984-2548-450a-9084-af3c19691254")
            param("system.cloud.profile_id", "amazon-1")
            param("total-work-time", "")
            param("description", "")
            param("cloud-code", "amazon")
            param("terminate-after-build", "true")
            param("enabled", "true")
            param("max-running-instances", "2")
            param("agentPushPreset", "")
            param("profileId", "amazon-1")
            param("name", "AWS Example")
            param("next-hour", "")
            param("secure:secret-key", "credentialsJSON:ab0415ff-68a8-497c-9311-e058f1917ebe")
            param("region", "us-east-1")
            param("terminate-idle-time", "30")
            param("not-checked", "")
        }
    }
}

object Build : BuildType({
    name = "Build and Test"

    artifactRules = "target => target"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        maven {
            goals = "clean package"
        }
        ideaInspections {
            pathToProject = "pom.xml"
            jvmArgs = "-Xmx512m -XX:ReservedCodeCacheSize=240m"
            targetJdkHome = "%env.JDK_18%"
        }
    }

    triggers {
        vcs {
        }
    }

    features {
        pullRequests {
            vcsRootExtId = "${DslContext.settingsRoot.id}"
            provider = github {
                authType = vcsRoot()
                filterTargetBranch = "+:refs/heads/main"
                filterAuthorRole = PullRequests.GitHubRoleFilter.EVERYBODY
            }
        }
        commitStatusPublisher {
            vcsRootExtId = "${DslContext.settingsRoot.id}"
            publisher = github {
                githubUrl = "https://api.github.com"
                authType = password {
                    userName = "dstewart-jetbrains"
                    password = "credentialsJSON:285d52d4-2750-421e-80b3-5e40f16f38ae"
                }
            }
        }
        merge {
            branchFilter = "+:pull/*"
            commitMessage = "Merge branch '%teamcity.build.branch%' by TeamCity"
        }
    }
})
