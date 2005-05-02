JAVA=`which java`
${JAVA} -classpath .:jmdns.jar:JSAP_1.03a.jar:swt.jar:swt-pi.jar -Djava.library.path=. Main
