#!/bin/sh
JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom"
JAVA_BIN="${JAVA_HOME}/bin/java"
cd betterrandom || exit 1
mvn -B -DskipTests -Darguments=-DskipTests -Dmaven.test.skip=true\
    clean package pre-integration-test install 2>&1
cd ../FifoFiller || exit 1
mvn -B package 2>&1
JAR=$(find target -iname '*-with-dependencies.jar')
mkfifo prng_out 2>&1

# Checked Dieharder invocation

chkdh() {
    dieharder -S 1 -g 200 $@ | tee /tmp/current_test.txt
    cat /tmp/current_test.txt >> dieharder.txt
    if [ "$(grep -m 1 'FAILED' dieharder.txt)" ]; then
      kill -9 "${JAVA_PROCESS}"
      exit 1
    fi
}
"${JAVA_BIN}" ${JAVA_OPTS} -jar "${JAR}" io.github.pr0methean.betterrandom.prng.${CLASS} prng_out ${SEED} 2>&1 &
JAVA_PROCESS=$!
(
  chkdh -Y 1 -k 2 -d 0
  chkdh -Y 1 -k 2 -d 1
  chkdh -Y 1 -k 2 -d 2
  chkdh -Y 1 -k 2 -d 3
  chkdh -Y 1 -k 2 -d 4
  chkdh -Y 1 -k 2 -d 5
  chkdh -Y 1 -k 2 -d 6
  chkdh -Y 1 -k 2 -d 7
  chkdh -Y 1 -k 2 -d 8
  chkdh -Y 1 -k 2 -d 9
  chkdh -Y 1 -k 2 -d 10
  chkdh -Y 1 -k 2 -d 11
  chkdh -Y 1 -k 2 -d 12
  chkdh -Y 1 -k 2 -d 13
  # Marked "Do Not Use": chkdh -Y 1 -k 2 -d 14
  chkdh -Y 1 -k 2 -d 15
  chkdh -Y 1 -k 2 -d 16
  chkdh -Y 1 -k 2 -d 17
  chkdh -Y 1 -k 2 -d 100
  chkdh -Y 1 -k 2 -d 101
  chkdh -d 102
  chkdh -Y 1 -k 2 -d 200 -n 1
  chkdh -Y 1 -k 2 -d 200 -n 2
  chkdh -Y 1 -k 2 -d 200 -n 3
  chkdh -Y 1 -k 2 -d 200 -n 4
  chkdh -Y 1 -k 2 -d 200 -n 5
  chkdh -Y 1 -k 2 -d 200 -n 6
  chkdh -Y 1 -k 2 -d 200 -n 7
  chkdh -Y 1 -k 2 -d 200 -n 8
  chkdh -Y 1 -k 2 -d 200 -n 9
  chkdh -Y 1 -k 2 -d 200 -n 10
  chkdh -Y 1 -k 2 -d 200 -n 11
  chkdh -Y 1 -k 2 -d 200 -n 12
  chkdh -Y 1 -k 2 -d 201 -n 2
  chkdh -Y 1 -k 2 -d 201 -n 3
  chkdh -Y 1 -k 2 -d 201 -n 4
  chkdh -Y 1 -k 2 -d 201 -n 5
  chkdh -Y 1 -k 2 -d 202
  chkdh -d 203 -p 40 -n 0
  chkdh -d 203 -p 40 -n 1
  chkdh -d 203 -p 40 -n 2
  chkdh -d 203 -p 40 -n 3
  chkdh -d 203 -p 40 -n 4
  chkdh -d 203 -p 40 -n 5
  chkdh -d 203 -p 40 -n 6
  chkdh -d 203 -p 40 -n 7
  chkdh -d 203 -p 40 -n 8
  chkdh -d 203 -p 40 -n 9
  chkdh -d 203 -p 40 -n 10
  chkdh -d 203 -p 40 -n 11
  chkdh -d 203 -p 40 -n 12
  chkdh -d 203 -p 40 -n 13
  chkdh -d 203 -p 40 -n 14
  chkdh -d 203 -p 40 -n 15
  chkdh -d 203 -p 40 -n 16
  chkdh -d 203 -p 40 -n 17
  chkdh -d 203 -p 40 -n 18
  chkdh -d 203 -p 40 -n 19
  chkdh -d 203 -p 40 -n 20
  chkdh -d 203 -p 40 -n 21
  chkdh -d 203 -p 40 -n 22
  chkdh -d 203 -p 40 -n 23
  chkdh -d 203 -p 40 -n 24
  chkdh -d 203 -p 40 -n 25
  chkdh -d 203 -p 40 -n 26
  chkdh -d 203 -p 40 -n 27
  chkdh -d 203 -p 40 -n 28
  chkdh -d 203 -p 40 -n 29
  chkdh -d 203 -p 40 -n 30
  chkdh -d 203 -p 40 -n 31
  chkdh -d 203 -p 40 -n 32
  chkdh -Y 1 -k 2 -d 204
  chkdh -d 205
  chkdh -d 206
  chkdh -d 207
  chkdh -d 208
  chkdh -d 209
) < prng_out
exit $?
