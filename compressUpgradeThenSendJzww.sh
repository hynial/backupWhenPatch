#!/bin/bash

preCompressDir=/Users/hynial/IdeaProjects/gm/backupWhenPatch/upgrade/2024-09-10_190249/
targetDir=/Users/hynial/0-jzww/1-patches/

versionName=$(basename ${preCompressDir})
# zip -qr "${targetDir}${versionName}.zip" ${preCompressDir}
cd "$preCompressDir"
cd ..
#pwd
zip -qr "${targetDir}${versionName}.zip" ./"${versionName}"
echo "${targetDir}${versionName}.zip"