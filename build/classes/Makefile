JCC = javac
JFLAGS = -g -d
GUIDIR = ./Run/
OUTGUI = ../build/classes/
CLEANDIR = ../build/classes/


default: clean TestClient.class TestServer.class

TestClient.class:
	$(JCC) $(JFLAGS) $(OUTGUI) $(GUIDIR)TestClient.java

TestServer.class:
	$(JCC) $(JFLAGS) $(OUTGUI) $(GUIDIR)TestServer.java

clean:
	$(RM) $(CLEANDIR)Run/*.class
	$(RM) $(CLEANDIR)Main/*.class
	$(RM) $(CLEANDIR)Threads/*.class
