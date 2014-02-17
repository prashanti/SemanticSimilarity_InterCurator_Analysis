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
import java.util.List;

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
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

public class Ancestors 
{

	public static void main(String[] args) throws IOException, OWLOntologyStorageException, OWLOntologyCreationException, SQLException, ParserException, ClassNotFoundException 
	{
		
		
		
		AncestorsMethods methods = new AncestorsMethods();
		String base="http://purl.obolibrary.org/obo/";
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	    OWLDataFactory factory = manager.getOWLDataFactory();
	    DbConnection dbconn=new DbConnection();
		Connection conn=dbconn.connection();
		String insertSQL = "INSERT IGNORE INTO tbl_ccexp_combination_ancestors_partofE" +
				" (term, ancestor) VALUES (?, ?)";
		PreparedStatement preparedStatement = conn.prepareStatement(insertSQL);
		preparedStatement.setString(1, "test");
		preparedStatement.setString(2, "test");
		preparedStatement.executeUpdate();
		
		
		
		
	    File file = new File("/Users/pmanda/Documents/charaparser-evaluation-data/ontologies-from-Prashanti" +
	    		"/NEW/AllOntologies_Jim_PartOfE.owl");
	    OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
	    System.out.println("Loaded ontology: " + ontology);
	    OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
	    OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
	    ShortFormProvider shortFormProvider = new SimpleShortFormProvider();
        DLQueryPrinter dlQueryPrinter = new DLQueryPrinter(new DLQueryEngine(reasoner, 
        		shortFormProvider), shortFormProvider);
	    reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);     
		
		BufferedReader br = new BufferedReader(new FileReader("/Users/pmanda/Documents/" +
				"charaparser-evaluation-data/annotations/AllAnnotations.txt"));
	    String line;
	    List<String> ontologies = Arrays.asList("GO","GOTEMP", "CL","PR","UBPROP","CHEBI-LITE", "FMA", 
	    		"UBERON", "UBERONTEMP", "BSPOTEMP", "PATOTEMP", "PATO", "BSPO", "UNKNOWNTEMP");
	    List<String> ids = new ArrayList<String>();
	    List<String> nestedids=new ArrayList<String>();
	   while ((line = br.readLine()) != null)
	    {
		   if (!line.trim().contains("Entity ID"))
		   {
		   line = line.replace(":", "_");
	    	line = line.replace("\"", "");
	    	line=methods.sub(line);
			String [] terms = line.split("\t",-1);
		    String e1=terms[4];
		    String q1=terms[6];
		    String e2=terms[8];
		    System.out.println(e1+" "+q1+" "+e2);
		    String expression=methods.getExpression(e1,q1,e2);
		    System.out.println("This"+expression);
		    if (expression != null)
		    {
		    String ancestors=methods.getExpressionAncestors(expression,dlQueryPrinter);
		    parseAncestor(ancestors,expression, conn, methods);
		    }
		    
		    
		    List<String> list = new ArrayList<String>();
		    list.add(e1);
		    list.add(q1);
		    list.add(e2);
			for ( String x: list )
			{
			  
			  
			    	if (x.contains("and") || x.contains("some"))
			    	{
			    		nestedids.add(x);
			    	}
			    
				
				
				
				if (x.contains("and"))
			    {
			    	x=x.replace("(", "");
			    	x=x.replace(")", "");
			    	String[] tempid = x.split("\\s+");
			    	for (String tmp: tempid)
			    	{
			    		String [] i = tmp.split("_");
			    	    String ID1= i[0];
			    		if (ontologies.contains(ID1)) 
						{
					        ids.add(tmp);
					    }
			    	}
			    }
			    else
			    {
			    	   String [] temp = x.split("_");
					    String ID=temp[0];
						if (ontologies.contains(ID)) 
						{
					        ids.add(x);
					    }
			    }
			 
			}
			
	    }
	    }
	   br.close();
			
       for (String classExpression: nestedids)
       {
           String ancestors= dlQueryPrinter.askQuery(classExpression.trim());
        	if (ancestors.trim().length() == 0 || ancestors.trim().equalsIgnoreCase(",owl:Thing"))
				{
        		System.out.println("No Ancestors Found"+ classExpression + "\t" +"\n");
        		
				}
        	parseAncestor(ancestors,classExpression, conn, methods);
       }
	   
	   
	   
	   for (String id: ids)
			{
				
				OWLClass clsA= factory.getOWLClass(IRI.create(base + id));
				String ancestors = methods.getancestors(clsA,reasoner);
				if (ancestors.trim().length() == 0 || ancestors.trim().equalsIgnoreCase(",owl:Thing"))
				{
					System.out.println("No Ancestors Found"+ clsA + "\t" +"\n");
				}
				parseAncestor(ancestors,id, conn, methods);
			
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
						preparedStatement.executeUpdate();
					}
				
				}			
			}
    }
}


