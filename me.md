# Project Description.
Patching the local changed files to a file list, which would be backup under a tomcat webapps directory.
## Steps
1. copy svn changed list to config file which named pathList.txt.
2. read pathList.txt by line.
3. transform each line to classpath source file path.
4. backup source file to target backup directory.

# Intellij IDEA - build a jar
## Config Project Structure For Create An Jar Artifacts:
    Artifacts > Add > Given Main Method, set extract libraries whether.
## Build The Artifacts From Menu : 
    Build > Build Artifacts > Choose Your Jar Artifact.