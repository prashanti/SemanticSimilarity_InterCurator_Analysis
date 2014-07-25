## computing character state level pairwise similarity. 
#inputs are two Classes-XXX files. 
def populateIC():
	global ic_dict
	f=open('IC-combination.txt','r')
	for line in f:
		data=line.split("\t")
		term=data[0].strip()
		ic=data[2].strip()
		if re.match('^[0-9.]+$',ic):
			ic_dict[term]=float(ic)
		else:
			print ic,term
			ic_dict[term]=ic


def getIC(anc1,anc2):
	global ic_dict
	common=set.intersection(anc1,anc2)
	maxIC=0
	t=""
	for term in common:
		ic=ic_dict[term.strip()]
		if ic > maxIC:
			maxIC=ic
			t=term	
	return (float(maxIC)/float(ic_dict['Maximum']))


def getancestors(line):
	#this will return a set
	global ancestor_dict
	line=line.strip()
	ancestors=ancestor_dict[line]
	return ancestors

def populateancestors(file):
	file=file.replace("Classes","Ancestors")	
	f = open(file,'r')
	global ancestor_dict
	for line in f:
		terms=line.split("\t")
		child=terms[0].strip()
		ancestor=terms[1].strip()
		if child in ancestor_dict:
			ancestor_dict[child].add(ancestor)
		else:
			ancestor_dict[child]=set()
			ancestor_dict[child].add(child)
			ancestor_dict[child].add(ancestor)

def populatemapping():
	global idmap_dict
	inp=open('id_mapping.txt','r')
	#1	s1e659a80-0e70-4a22-94a1-53f3672868e2
	#1_0	s69d469cb-64a0-4f1d-9785-c55f20383bb0
	for line in inp:
		temp=line.split("\t")
		idmap_dict[temp[0].strip()]=temp[1].strip()


def main():
	# input files are class files like Classes-40674.txt or New-Classes-40674.txt
	out=open("LatexTableOut.txt",'a')
	global characterscoresIC
	global characterscoresSimJ
	inputfile1="Classes-"+str(sys.argv[1])
	inputfile2="Classes-"+str(sys.argv[2])
	qualities=int(sys.argv[3])
	file1=open(inputfile1,'r')
	populateancestors(inputfile1)
	populateancestors(inputfile2)
	populateIC()
	IClist=[]
	SimJlist=[]
	Linlist=[]
	line1count=1
	pereqscorefile="./CombinedComparisons/"+"PerEQ_"+inputfile1.replace(".tsv","").replace("Classes-","")+ "--"+inputfile2.replace("NR--","").replace("Transformed_","").replace("KR--","").replace(".tsv","").replace("Classes-","")+".tsv"
	if qualities==1:
		outputfile="./CombinedComparisons/"+inputfile1.replace(".tsv","").replace("Classes-","")+ "--"+inputfile2.replace("NR--","").replace("Transformed_","").replace("KR--","").replace(".tsv","").replace("Classes-","")+".tsv"
	else:
		outputfile="./CombinedComparisons/NoQualitites_"+inputfile1.replace(".tsv","").replace("Classes-","")+ "--"+inputfile2.replace("NR--","").replace("Transformed_","").replace("KR--","").replace(".tsv","").replace("Classes-","")+".tsv"
	


	
	pereq=open(pereqscorefile,'w')
	outputfile=outputfile.replace(".txt","")
	res=open(outputfile,'w')
	res.write("Character Number"+"\t"+"State Number"+"\t"+"SimJ Score" +"\t"+"NIC Score"+"\n")
	pereq.write("Character Number"+"\t"+"State Number"+"\t"+ "Curator 1 EQ Number"+"\t"+"Curator 2 EQ Number"+"\t"+ "SimJ Score"+"\t"+"NIC Score"+ "\n")
	for line1 in file1:
		file2=open(inputfile2,'r')
		line2count=1
		line1count=line1count+1
		for line2 in file2:
			
			
			line2count=line2count+1
			
			#format is character\tstate\tclassname
			# do the below only if the characters and the states are the same. 
			data1=line1.split("\t")
			data2=line2.split("\t")
			character1=data1[0].strip()
			state1=data1[1].strip()
			character2=data2[0].strip()
			state2=data2[1].strip()
			class1=data1[2].strip()
			class2=data2[2].strip()
			eqnumber1=data1[3].strip()
			eqnumber2=data2[3].strip()
			
			key=character1+"\t"+state1



			simj=0
			ic=0
			if character1 == character2 and state1 == state2:
				if class1.strip() !="" and class2.strip()!="":
					#print "C "+character1+" S "+state1+ " class1 "+class1+" class2 "+class2
					#print " doing curator1 character "+character1+ " state "+state1+" doing curator2 character "+ character2+ " state2 "+state2
					ancestors1=getancestors(class1) #this is a set
					ancestors2=getancestors(class2)
					ic=getIC(ancestors1,ancestors2)
					normicmean=str(round(float(numpy.mean(IClist))/float(ic_dict['Maximum']),3))
					unionset=set.union(ancestors1,ancestors2)
					commonancestors= set.intersection(ancestors1,ancestors2)
	
					simj=float(len(commonancestors))/float(len(unionset))

				else:
					simj=0	
				pereq.write(str(character1)+"\t"+str(state1)+"\t"+eqnumber1+"\t"+eqnumber2+  "\t"+str(simj)+"\t"+str(ic)+"\n")
				characterscoresSimJ[key].append(simj)
				characterscoresIC[key].append(ic)

				
				
	#get the max values for each character-state comparison here and add to IClist and SimJ list			

	for characterstate in characterscoresSimJ:
		tmp=characterstate.split("\t")
		character=tmp[0]
		state=tmp[1]

		icmaxscore=max(characterscoresIC[characterstate])
		IClist.append(icmaxscore)

		simjmaxscore=max(characterscoresSimJ[characterstate])
		SimJlist.append(simjmaxscore)

		res.write(character+"\t"+state+"\t"+str(simjmaxscore)+"\t"+str(icmaxscore)+"\n")
		

	



	simjmean=str(round(numpy.mean(SimJlist),3))	
	simjmedian=str(round(numpy.median(SimJlist),3))
	normicmean=str(round(numpy.mean(IClist),3))
	normicmedian=str(round(numpy.median(IClist),3))

	print "\n\n"+outputfile.replace("CombinedComparisons","").replace(".//","").replace("Classes-","")
	print "\tMean SimJ",simjmean
	print "\tMedian SimJ",simjmedian

	print "\tMean IC",normicmean
	print "\tMedian IC",normicmedian
	
	
	outfile=outputfile.replace("CombinedComparisons","").replace(".//","").replace("Classes-","")
	out.write(outfile+ " & "+simjmean+" & "+simjmedian+" & "+normicmean+" & "+normicmedian +" \\\\"+"\n")







if __name__ == "__main__":
	from collections import defaultdict
	characterscoresIC = defaultdict(list)
	characterscoresSimJ = defaultdict(list)
	import numpy
	import sys
	import math
	import re
	ancestor_dict=dict()
	ic_dict={}
	idmap_dict={}
	main()