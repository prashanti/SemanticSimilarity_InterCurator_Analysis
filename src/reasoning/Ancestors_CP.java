package reasoning;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

public class Ancestors_CP 
{

	public static void main(String[] args) throws IOException, OWLOntologyStorageException, OWLOntologyCreationException, SQLException, ParserException, ClassNotFoundException 
	{
		AncestorsMethods methods = new AncestorsMethods();
		String base="http://purl.obolibrary.org/obo/";
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	    OWLDataFactory factory = manager.getOWLDataFactory();
	    File file = new File("/Users/pmanda/Documents/charaparser-evaluation-data/ontologies-from-Prashanti/" +
	    		"NEW/AllOntologies_Jim.owl");
	    OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
	    System.out.println("Loaded ontology: " + ontology);
	    OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
	    OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
	    reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
	    DbConnection dbconn=new DbConnection();
		Connection conn=dbconn.connection();     
		BufferedReader br = new BufferedReader(new FileReader("/Users/pmanda/" +
				"Documents/charaparser-evaluation-data/annotations/CharaParser-EQ-generator/CP_AllAnnotations.xls"));
		 ShortFormProvider shortFormProvider = new SimpleShortFormProvider();
	        DLQueryPrinter dlQueryPrinter = new DLQueryPrinter(new DLQueryEngine(reasoner, shortFormProvider), shortFormProvider);
	       
		String line;
	    List<String> ontologies = Arrays.asList("GO","CL","PR","UBPROP","CHEBI-LITE", "FMA", "UBERON", "UBERONTEMP", "BSPOTEMP", "PATOTEMP", "PATO", "BSPO", "UNKNOWNTEMP");
	    List<String> ids = new ArrayList<String>();
	    List<String> nestedids = new ArrayList<String>();
	   while ((line = br.readLine()) != null)
	    {
		
		
	    	line = line.replace(":", "_");
	    	line = line.replace("\"", "");
	    	line = line.replace(",", "@,");
	    	line = line.replace("@@,", "@,");
	    	
	    	line=line.replace("PATO_0000586PATO_0001602","PATO_0000586@,PATO_0001602"); 
		    line=methods.sub(line);
		
		    
			String [] terms = line.split("\t",-1);
		    String e1=terms[3];
		    String q1=terms[4];
		    String e2=terms[5];
		    
		    System.out.println("Line"+line);
		    processExpression(e1, e2,  q1, line, methods,
					dlQueryPrinter, conn);
		    
		    List<String> list = new ArrayList<String>();
		    list.add(e1);
		    list.add(q1);
		    list.add(e2);
		    
		    for (String str:list)
		    {
		    if (str.contains(","))
		    {
		    	String temp[]=e1.split("@,");
		    	for (String y:temp)
		    	{
		    	String tmp[]=y.split("Score");
		    	for (String id:tmp)
		    	{
		    		if (!(id.contains("PHENOSCAPE_complement_of some BSPO_0000099")) & !(id.contains("null")) &  (id.contains("some") || (id.contains("and"))))
		    		{
		    			nestedids.add(id);
		    		}
		    	}
		    	}
		    }
		    
		    else
		    {
		    	//UBERON:0009125 Score:[0.5]
		    	String tmp[]=str.split("Score");
		    	for (String id:tmp)
		    	{
		    		if (!(id.contains("PHENOSCAPE_complement_of some BSPO_0000099"))  & !(id.contains("null")) &  (id.contains("some") || (id.contains("and"))))
		    		{
		    			nestedids.add(id);
		    		}
		    	}
		    }
		    }
		    
		    
			for ( String x: list )
			{
				x = x.replace("@,", " ");
				x=x.replace("(", "");
		    	x=x.replace(")", "");
		    	String[] tempid = x.split("\\s+");
			    for (String ontID: tempid)
			    	{
			    	String[] temp = ontID.split("_");
			    	String id=temp[0];
			    	if (ontologies.contains(id))
			    	{
			    		
			    		System.out.println("ID"+ontID);
			    		ids.add(ontID);
			    	}
			    	}
			    	

			}
	    }
	   br.close();
			for (String id: ids)
			{
				id=id.trim();
				OWLClass clsA= factory.getOWLClass(IRI.create(base + id));
				String ancestors = getancestors(clsA,reasoner);
				if (ancestors.trim().length() == 0 || ancestors.trim().equalsIgnoreCase(",owl:Thing"))
				{
					System.out.println("No Ancestors Found"+ id + "\t" +"\n");
				}
				
				 parseAncestor(ancestors, id, conn, methods);
				
			}	
	    
	
	
       for (String classExpression: nestedids)
       {
           String ancestors= dlQueryPrinter.askQuery(classExpression.trim());
           classExpression=methods.reverseSub(classExpression);
            
            if (ancestors.trim().length() == 0 || ancestors.trim().equalsIgnoreCase(",owl:Thing"))
			{
				System.out.println("No Ancestors Found"+ classExpression + "\t" +"\n");
			}
            parseAncestor(ancestors, classExpression, conn, methods);
       }
       
	}
	
	public static void processExpression(String e1, String e2, String q1, String line, AncestorsMethods methods,
			DLQueryPrinter dlQueryPrinter, Connection conn) throws ParserException, SQLException
	{
		String [] entitylist= e1.split("@,",-1);
	    String [] entity2list= e2.split("@,",-1);
	    String [] qualitylist= q1.split("@,",-1);
	    ArrayList<Integer> lenList = new ArrayList<Integer>();
	    lenList.add(entitylist.length); 
	    lenList.add(entity2list.length); 
	    lenList.add(qualitylist.length); 
	    Integer MAX = Collections.max(lenList);
	   
	    for (int i=0 ; i<MAX ; i++)
	    {
	    	String expression = null;
	    	String en=null;
	    	String qu=null;
	    	String en2 = null;
	    	
	    	if (i < entitylist.length)
	    	{
	    		en =entitylist[i].split("Score",-1)[0];
	    		if (en.length()==0) {en = null;}
	    	}
	    	
	    	if (i < qualitylist.length)
	    	{
	    		qu =qualitylist[i].split("Score",-1)[0];
	    		if (qu.trim().length()==0) {qu = null;}
	    	}
	    	
	    	
	    	if (i < entity2list.length)
	    	{
	    		en2 =entity2list[i].split("Score",-1)[0];
	    		if (en2.length()==0) {en2 = null;}
	    	}
	    	if (qu !=null  && (qu.contains("RO_") || qu.contains("BSPO") || qu.contains("BFO") || qu.contains("PHENOSCAPE")) && !(qu.contains("and") || 
	    			qu.contains("some")))
	    	{
	    		en2 = qu+" some ("+en2+")";
	    		qu=null;
	    		System.out.println(en2);
	    	}
	    	
	    	
	    	if (!line.contains("null") &&  qu != null )
	    	{
	    	System.out.println("This "+"Q "+qu+"E "+en+"E2 "+en2);	
	    	expression = methods.getExpression(en,qu,en2);
			System.out.println("Expression"+ expression);
			String ancestors= dlQueryPrinter.askQuery(expression.trim());
			if (ancestors.trim().length() == 0 || ancestors.trim().equalsIgnoreCase(",owl:Thing"))
			{
				System.out.println("No Ancestors Found"+ expression + "\t" +"\n");
			}
			parseAncestor(ancestors,expression,conn, methods);
			
	    	}
	    	
	    	
	    }
	}
    public static  void parseAncestor(String list, String classExpression, Connection conn, AncestorsMethods methods) throws SQLException
    {
    	String [] temp=list.split(",");
    	System.out.println("This"+classExpression);
     	classExpression=methods.reverseSub(classExpression);
			for (String anc: temp)
			{
				anc=anc.trim();
			
				if (anc.length() != 0 && !(anc.equalsIgnoreCase("owl:Thing")))
				{
					
					String [] anclist=anc.split("/obo/");
					String ancestor=anclist[1];
					ancestor=methods.reverseSub(ancestor);
					
				
					if (ancestor.length()!=0)
					{	
						ancestor=ancestor.replace(">","");
						System.out.println(classExpression+"  "+ancestor);
						String insertSQL = "INSERT IGNORE INTO tbl_ccexp_combination_ancestors_partofE" +
								" (term, ancestor) VALUES (?, ?)";
						PreparedStatement preparedStatement = conn.prepareStatement(insertSQL);
						preparedStatement.setString(1, classExpression);
						preparedStatement.setString(2, ancestor);
						//preparedStatement.executeUpdate();
					}
				
				}			
			}
    }   
	public static String getancestors(OWLClass clsA, OWLReasoner reasoner) throws OWLOntologyCreationException
	    {
	    	String ancestorlist="";
	    	NodeSet<OWLClass> subClses = reasoner.getSuperClasses(clsA, false);
	        Set<OWLClass> clses = subClses.getFlattened();
	        for (OWLClass cls : clses) {
	            ancestorlist=ancestorlist+","+cls;
	        }
	        //System.out.println(ancestorlist);
	        return(ancestorlist);
	    }

}

