# Adapt the two following lines -- NB: Java 8 is required
JAVA_HOME="C:/dev/Java/JDK8"
GRADLE_EXE_PATH="../gradlew"
# These 2 instruct the XMCDA Java lib to use a specific version when writing
export XMCDAv2_VERSION=2.2.2
export XMCDAv3_VERSION=3.1.0

# -- You normally do not need to change anything beyond this point --
JAR_PATH="./build/libs" #default path where gradle puts outputs, however may be changed later as same as jar name if necessary
VERSION="1.0"
PROJECT_NAME="{project}"
JAR_FILE_NAME="$PROJECT_NAME-$VERSION.jar"

JAVA="${JAVA_HOME}/bin/java"

export JAVA_HOME
if [[ ! -f ${JAR_PATH}/${JAR_FILE_NAME} ]]; then
    if [[ ! -f ${GRADLE_EXE_PATH} ]]; then
        echo "Please modify common_settings.sh to reflect your gradle installation (see README)" >&2
            exit -1;
    fi
    ${GRADLE_EXE_PATH} :${PROJECT_NAME}:shadowJar
fi

CMD="${JAVA} -jar ${JAR_PATH}/${JAR_FILE_NAME}"
