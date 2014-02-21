p = frHJLib/
cp = -classpath .:habanero-java-lib.jar
c = .class
j = .java
jc = javac -source 8

all: compile run

compile: $(p)ImageRegion$(c) $(p)SourceImageRegion$(c) $(p)Master$(c) $(p)MatchMaker$(c) $(p)ImagePanel$(c) $(p)FaceReplace$(c) $(p)Main$(c)

$(p)ImageRegion$(c): $(p)ImageRegion$(j)
	javac -source 8 $(p)ImageRegion$(j)

$(p)SourceImageRegion$(c): $(p)SourceImageRegion$(j) $(p)ImageRegion$(c)
	javac -source 8 $(p)SourceImageRegion$(j)

$(p)Master$(c): $(p)Master$(j) $(p)SourceImageRegion$(c)
	$(jc) $(cp) $(p)Master$(j)

$(p)MatchMaker$(c): $(p)MatchMaker$(j) $(p)Master$(c)
	$(jc) $(cp) $(p)MatchMaker$(j)

$(p)ImagePanel$(c): $(p)ImagePanel$(j) $(p)MatchMaker$(c)
	$(jc) $(p)ImagePanel$(j)

$(p)FaceReplace$(c): $(p)FaceReplace$(j) $(p)ImagePanel$(c)
	$(jc) $(p)FaceReplace$(j)

$(p)Main$(c): $(p)Main$(j) $(p)FaceReplace$(c)
	$(jc) $(cp) $(p)Main$(j)

run:
	java $(cp) $(p)Main

clean:
	rm -f $(p)*$(c)