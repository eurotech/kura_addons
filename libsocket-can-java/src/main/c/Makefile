# Compile time specfied options
TARGET_OPTS=x86,armv5_sf,armv6_hf

# Library Definitions
JNI_CLASSES=de.entropia.can.CanSocket
JAVAC_FLAGS=-g -Xlint:all
JAVA_SRC:=$(shell find ../java -type f -and -name '*.java')
JNI_SRC:=$(shell find jni -type f -and -regex '^.*\.\(cpp\|h\)$$')
SONAME=jni_socketcan

# Directory locations
JAVA_DEST=../../../target/jni/classes
JNI_DIR=../../../target/jni/headers
JAR_DEST=../../../target/jni/dist

all: jni_src
	mv $(LIB_DEST)/lib$(SONAME).so ../../../lib/linux/$(TARGET)/

init:
    ### JAVA_HOME
    ifndef JAVA_HOME
        JAVA_HOME=$(shell readlink -f /usr/bin/javac | sed "s:bin/javac::")
    endif
    JAVAH=$(JAVA_HOME)/bin/javah
    JAVAC=$(JAVA_HOME)/bin/javac
    JAVA_INCLUDES=-I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux

    ### TARGET
    ifndef TARGET
	@echo "TARGET must be specified.  These can be: $(TARGET_OPTS)"; exit 2;
    else
        ifeq ($(TARGET),x86)
            CXX=g++
            STRIP=strip
            CXXFLAGS=-I./include -I$(JNI_DIR) -O2 -g -pipe -Wall -Wp,-D_FORTIFY_SOURCE=2 -fexceptions -fstack-protector --param=ssp-buffer-size=4 -fPIC -Wno-unused-parameter -pedantic -D_REENTRANT -D_GNU_SOURCE $(JAVA_INCLUDES)
            LDFLAGS=-Wl,-soname,$(SONAME)
            LIB_DEST=../../../target/jni/lib
        else ifeq ($(TARGET),armv5_sf)
            CXX=g++
            STRIP=strip
            CXXFLAGS=-I./include -I$(JNI_DIR) -O2 -g -pipe -Wall -Wp,-D_FORTIFY_SOURCE=2 -fexceptions -fstack-protector --param=ssp-buffer-size=4 -fPIC -Wno-unused-parameter -pedantic -D_REENTRANT -D_GNU_SOURCE $(JAVA_INCLUDES)
            LDFLAGS=-Wl,-soname,$(SONAME)
            LIB_DEST=../../../target/jni/lib
        else ifeq ($(TARGET),armv6_hf)
            CXX=g++
            STRIP=strip
            CXXFLAGS=-I./include -I$(JNI_DIR) -O2 -g -pipe -Wall -Wp,-D_FORTIFY_SOURCE=2 -fexceptions -fstack-protector --param=ssp-buffer-size=4 -fPIC -Wno-unused-parameter -pedantic -D_REENTRANT -D_GNU_SOURCE $(JAVA_INCLUDES)
            LDFLAGS=-Wl,-soname,$(SONAME)
            LIB_DEST=../../../target/jni/lib
        else
	    @echo "TARGET must be specified.  These can be: $(TARGET_OPTS)"; exit 2;
        endif
    endif

    # DIRS to be created for the build
    DIRS=$(JAVA_DEST) $(LIB_DEST) $(JAR_DEST)

java_src: init
	### Make directories
	mkdir -p $(DIRS)

	$(JAVAC) $(JAVAC_FLAGS) -d $(JAVA_DEST) $(sort $(JAVA_SRC))

jni_src: java_src
	$(JAVAH) -jni -d $(JNI_DIR) -classpath $(JAVA_DEST) $(JNI_CLASSES)
	$(CXX) $(CXXFLAGS) $(LDFLAGS) -shared -o $(LIB_DEST)/lib$(SONAME).so $(sort $(filter %.cpp,$(JNI_SRC)))
	$(STRIP) $(LIB_DEST)/lib$(SONAME).so
