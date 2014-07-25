
def main():

	ancestors_dict={}
	infile=str(sys.argv[1])
	outfile=str(sys.argv[2])
	f=open(infile,'r')
	w=open(outfile,'w')
	corpussize=0
	for line in f:
		corpussize=corpussize+1
		line=line.strip()
		if line in ancestors_dict:
			ancestors_dict[line]=ancestors_dict[line]+1
		else:
			ancestors_dict[line]=1
	f=open(infile,'r')
	print "Corpus size "+str(corpussize)
	p=float(1)/float(corpussize)
	maxic=-math.log(p,2)
	w.write("Maximum"+"\t"+""+"\t"+str(maxic)+"\n")
	for line in f:
		freq=ancestors_dict[line.strip()]
		p=float(freq)/float(corpussize)
		ic=-math.log(p,2)
		w.write(line.strip()+"\t"+str(freq)+"\t"+str(ic)+"\n")


		
if __name__ == "__main__":
	import math
	import sys
	main()

