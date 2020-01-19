# Use NIO in gradle

maven { url "https://github.com/eGit/maven-repo-nio/raw/master" } // or "https://raw.githubusercontent.com/eGit/maven-repo-nio/master"

implementation "de.genflux:nio:0.1"



# Update repo

edit build.gradle with new version of NIO and execute: gradle publishAllPublicationsTo-My-Repo-Repository

git add -A
git commit -m "new NIO version 0.1"
git push