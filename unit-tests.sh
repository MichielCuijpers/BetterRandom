#!/bin/sh
if [ "$ANDROID" = 1 ]; then
  MAYBE_ANDROID_FLAG="-Pandroid"
else
  MAYBE_ANDROID_FLAG=""
fi
cd betterrandom
# Coverage test
mvn $MAYBE_ANDROID_FLAG clean jacoco:prepare-agent test jacoco:report -e
STATUS=$?
if [ "$STATUS" = 0 ]; then
  if (([ "$TRAVIS" = "true" ] && [ "$TRAVIS_JDK_VERSION" != "oraclejdk9" ]) || [ "$APPVEYOR" != "" ] ); then
    git clone https://github.com/Pr0methean/betterrandom-coverage.git
    cd betterrandom-coverage
    if [ "$TRAVIS" = "true" ]; then
      COMMIT="$TRAVIS_COMMIT"
      JOBID="travis_$TRAVIS_JOB_NUMBER"
      git config --global user.email "travis@travis-ci.org"
    else
      GH_TOKEN=`powershell 'Write-Host ($env:access_token) -NoNewLine' `
      COMMIT="$APPVEYOR_REPO_COMMIT"
      JOBID="appveyor_$APPVEYOR_JOB_NUMBER"
      git config --global user.email "appveyor@appveyor.com"
    fi
    mkdir -p "$COMMIT/target"
    mv ../target/jacoco.exec "$COMMIT/target/$JOBID.exec"
    cd "$COMMIT"
    git add .
    git commit -m "Coverage report from job $JOBID"
    git remote add originauth "https://${GH_TOKEN}@github.com/Pr0methean/betterrandom-coverage.git"
    git push --set-upstream originauth master
    mv ../../travis-resources/jacoco_merge.xml $COMMIT/pom.xml
    while [ ! $? ]; do
      git pull --commit  # Merge
      git push
    done
    mvn jacoco:merge jacoco:report
    mv target/* ../../target
    cd ../..
    if [ "$TRAVIS" = "true" ]; then
      # Coveralls doesn't seem to work in non-.NET Appveyor yet
      # so we have to hope Appveyor pushes its Jacoco reports before Travis does! :(
      mvn coveralls:report
      
      # Send coverage to Codacy
      wget 'https://github.com/codacy/codacy-coverage-reporter/releases/download/2.0.0/codacy-coverage-reporter-2.0.0-assembly.jar'
      java -jar codacy-coverage-reporter-2.0.0-assembly.jar -l Java -r target/site/jacoco/jacoco.xml
    fi
  fi
  if [ "$TRAVIS_JDK_VERSION" != "oraclejdk9" ]; then
    # Proguard is disabled on JDK9 due to:
    # https://sourceforge.net/p/proguard/feature-requests/181/
    # https://sourceforge.net/p/proguard/bugs/551/
    mvn -DskipTests $MAYBE_ANDROID_FLAG package && (
    # Post-Proguard test (verifies Proguard settings)
    mvn $MAYBE_ANDROID_FLAG test -e)
    STATUS=$?
  fi
fi
cd ..
exit "$STATUS"