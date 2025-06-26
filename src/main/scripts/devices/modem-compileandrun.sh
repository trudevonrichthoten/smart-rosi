javac -cp /usr/share/java/RXTXcomm.jar:. TwoWaySerialComm.java
java -Djava.library.path=/usr/lib/jni -cp /usr/share/java/RXTXcomm.jar:. TwoWaySerialComm /dev/ttyUSB0
