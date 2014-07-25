def getName(term):
	term=term.replace(" ","")
	term=term.replace("(","")
	term=term.replace(")","")
	term=term.replace("and","")
	term=term.replace("some","")
	term=term.replace("\t","")
	return(term.strip())

def getExpression(E1,Q1,E2):
	if "Entity ID" in E1:
		return ("null")

	expression="null"
	if len(Q1)!=0:
		expression = Q1
	
	if len(E1)!=0:
		if expression != "null":
			expression=expression+" and inheres_in some ("+E1+")"
		
		else:
			expression="inheres_in some ("+E1+")"
		
	
	if len(E2)!=0:
		if expression != "null":
			expression=expression+" and towards some ("+E2+")"
		
		else:
			expression="towards some ("+E2+")"

	return(expression)



def getancestors(term,cc,database):
	term=term.strip()
	#NOTE: The term itself needs to be added to the list of ancestors
	global ancestor_dict

	if term in ancestor_dict:
		if term not in ancestor_dict[term]:
			ancestor_dict[term].append(term)
	else:
		ancestor_dict[term]=[term]
	query="select ancestor from "+database+" where term = "+ "\""+term+"\""
	cc.execute(query)	
	#cc.execute("select ancestor from tbl_ccexp_combination_ancestors_partofE where term = %s",term)
	data = cc.fetchall()
	for row in data:
		ancestor=row[0]
		if term in ancestor_dict:
			if ancestor not in ancestor_dict[term]:
				ancestor_dict[term].append(ancestor)
		else:
			ancestor_dict[term]=[ancestor]
			if term not in ancestor_dict[term]:
				ancestor_dict[term].append(term)

def getqueryresult(term,cc,database):
	query="select ancestor from "+database+" where term = "+ "\""+term+"\""
	cc.execute(query)	
	#cc.execute("select ancestor from tbl_ccexp_combination_ancestors_partofE where term = %s",term)
	data = cc.fetchall()
	ancestorlist=""
	for row in data:
		ancestorlist=ancestorlist+","+row[0]
	return ancestorlist



def main():

	# inputfile is original annotation file like 40674.txt
	import MySQLdb
	import sys
	inputfile=str(sys.argv[1])
	qualities=int(sys.argv[2])
	database=sys.argv[3]
	source=str(sys.argv[4])
	eqnumber={}
	db = MySQLdb.connect("localhost","root","","ontologies")
	cursor = db.cursor()
	w=open('AllAncestors_Combinations.txt','a')
	ANN=open(inputfile,'r')
	ancestorfile="Ancestors-"+inputfile
	PUT=open(ancestorfile,'w')
	classfile="Classes-"+inputfile
	Out=open(classfile,'w')
	masterfile="Master-"+inputfile
	master=open(masterfile,'w')
	groupingclassesfile="GroupingClasses-"+inputfile
	group=open(groupingclassesfile,'w')
	masterancestor=set()
	count=0
	Individual=inputfile.replace(".txt","")
	for line in ANN:
		ancestorgroup=set()
		if "Character" not in line:
			line=line.replace(":","_")
			terms=line.split("\t")
			E1= terms[4].strip()
			if qualities ==1:
				Q1 = terms[6].strip()
			else:
				Q1=""
			E2=terms[8].strip()
			character=terms[0].strip()
			state=terms[2].strip()
			key=str(character)+"_"+str(state)
			if key not in eqnumber:
				eqnumber[key]=0
			if (( "RO_" in Q1 or "BSPO" in Q1 or "BFO" in Q1 or "PHENOSCAPE" in Q1) and not("and"  in Q1 or 
		    			"some"  in Q1)):
				if E2 !="null" and E2.strip() !="":
					E1 = Q1+" some ("+E2+")"
					Q1=""
			child=getName(E1+Q1+E2)
			w.write(child.strip()+"\n")
			if "null" not in child:
				expression=getExpression(E1,Q1,E2)
				if expression != "null":
					topclass= getqueryresult(expression,cursor,database)
					if len(topclass)==0:
						#print "No Anc"+expression
						1
					anclist=topclass.split(",")
					for anc in anclist:
						if anc != "":
							group.write(Individual+"\t"+anc+"\n")
							ancestorgroup.add(getName(anc))
							masterancestor.add(getName(anc))
							w.write(anc.strip()+"\n")
				
				eqnumber[key]+=1
				eqnumberstring=source+str(eqnumber[key])			
				Out.write(character+"\t"+state+"\t"+child+"\t"+eqnumberstring+"\n")


				if E1:
					if E1 not in ancestor_dict:
						getancestors(E1,cursor,database)
					

						
				if E2:
					if E2 not in ancestor_dict:
						getancestors(E2,cursor,database)
						

				if Q1:
					if Q1 not in ancestor_dict:
						getancestors(Q1,cursor,database)
					

				if Q1 and E1 and E2:

					for e1 in ancestor_dict[E1]:
						for q1 in ancestor_dict[Q1]:
							for e2 in ancestor_dict[E2]:
									e1=e1.strip()
									q1=q1.strip()
									e2=e2.strip()
									ancestorgroup.add(getName(e1+q1+e2))
									ancestorgroup.add(getName(e1+q1))
									w.write(getName(e1+q1+e2).strip()+"\n")
									w.write(getName(e1+q1).strip()+"\n")
									
				elif E1 and Q1:

					for e1 in ancestor_dict[E1]:
						for q1 in ancestor_dict[Q1]:
							e1=e1.strip()
							q1=q1.strip()
							ancestorgroup.add(getName(e1+q1))
							w.write(getName(e1+q1).strip()+"\n")
							

				elif Q1 and E2:

					for e2 in ancestor_dict[E2]:
						for q1 in ancestor_dict[Q1]:
							e2=e2.strip()
							q1=q1.strip()
							ancestorgroup.add(getName(e2+q1))
							w.write(getName(e2+q1).strip()+"\n")
				elif E1 and E2:
					for e1 in ancestor_dict[E1]:
						for e2 in ancestor_dict[E2]:
								e1=e1.strip()
								e2=e2.strip()
								ancestorgroup.add(getName(e1+e2))
								ancestorgroup.add(getName(e1))
								w.write(getName(e1+e2).strip()+"\n")
								w.write(getName(e1).strip()+"\n")					
				elif E1:
					
					for e1 in ancestor_dict[E1]:
						e1=e1.strip()
						ancestorgroup.add(e1)
						w.write(getName(e1).strip()+"\n")
				elif E2:
					
					for e2 in ancestor_dict[E2]:
						e2=e2.strip()
						ancestorgroup.add(e2)
						w.write(getName(e2).strip()+"\n")
				elif Q1:
					
					for q1 in ancestor_dict[Q1]:
						q1=q1.strip()
						ancestorgroup.add(q1)
						w.write(getName(q1).strip()+"\n")
				else:
					1


				for anc in ancestorgroup:
					PUT.write(child+"\t"+getName(anc)+"\n")
  			else:
  				eqnumber[key]+=1
  				#null is in the expression like "null and" 
  				eqnumberstring=source+str(eqnumber[key])
				Out.write(character+"\t"+state+"\t"+" "+ "\t"+eqnumberstring+  "\n")
					
if __name__ == "__main__":
    ancestor_dict={}
    main()
