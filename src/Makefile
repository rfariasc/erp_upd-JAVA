JFLAGS = -g
JC = javac
RM = rm

.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

# Compile
SOURCES = \
	erp_udp.java \
	Global.java \
	Host.java
	
CLASSES = \
        erp_udp.class \
        Global.class \
        Host.class
        
default: classes

classes: $(SOURCES:.java=.class)

clean:
	$(RM) *.class $(JARNAME)

run:
	java Main