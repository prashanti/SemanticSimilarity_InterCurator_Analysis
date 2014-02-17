package reasoning;

import java.sql.SQLException;
import java.util.Set;

import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public class AncestorsMethods {
	
	public String getancestors(OWLClass clsA, OWLReasoner reasoner) throws OWLOntologyCreationException
    {
    	String ancestorlist="";
    	NodeSet<OWLClass> subClses = reasoner.getSuperClasses(clsA, false);
        Set<OWLClass> clses = subClses.getFlattened();
        for (OWLClass cls : clses) {
            ancestorlist=ancestorlist+","+cls;
        }
        return(ancestorlist);
    }
	
	public String getExpressionAncestors(String expression,DLQueryPrinter dlQueryPrinter) throws SQLException, ParserException
	{
		  
		   String ancestors1="";
		    if (expression != null)
		    {
		    ancestors1= dlQueryPrinter.askQuery(expression.trim());
		    if (ancestors1.trim().length() == 0 || ancestors1.trim().equalsIgnoreCase(",owl:Thing"))
			{
		    	System.out.println("No Ancestors Found"+ expression.trim() + "\t" +"\n");
		    	
			}
		    
		    
	}
		    return(ancestors1);
	}
    public String getExpression(String E1, String Q1, String E2)
    {
    
    	if (E1 != null && E1.equals("Entity ID"))
    	{
    		return (null);
    	}
    	String expression=null;
    	if (Q1 != null && Q1.trim().length()!=0)
    	{
    		expression = Q1; 
    	}
    	
    	
    	if (E1 !=null && E1.trim().length()!=0)
    	{
    		if (expression != null)
    		{
    			expression=expression+" and inheres_in some ("+E1+")";
    		}
    		else
    		{
    			expression="inheres_in some ("+E1+")";
    		}
    	}
    	
    	if (E2 != null && E2.trim().length()!=0)
    	{
    		if (expression != null)
    		{
    			expression=expression+" and towards some ("+E2+")";
    		}
    		else
    		{
    			expression="towards some ("+E2+")";
    		}
    	}	

    	
    	return(expression);
    }

	

    
    public  String reverseSub(String line)
	{
		
		
		
		line=line.replace("passes_through","BSPO_passes_through");
		line=line.replace("RO_0001025","OBO_REL_located_in");
		line=line.replace("UBREL_0000001","RO_0002150");
		line=line.replace("has_muscle_insertion","UBERON_has_muscle_insertion");
		line=line.replace("BSPO_0000126","UBERON_in_lateral_side_of");
		line=line.replace("has_muscle_origin","UBERON_has_muscle_origin");
		line=line.replace("encloses","UBERON_encloses");
		line=line.replace("attaches_to","UBERON_attaches_to");
		line=line.replace("connects","UBERON_connects");
		line=line.replace("BSPO_0000127","UBERON_in_median_plane_of");
		line=line.replace("posteriorly_connected_to","UBERON_posteriorly_connected_to");
		line=line.replace("anteriorly_connected_to","UBERON_anteriorly_connected_to");
		return(line);
	}
    
	public  String sub(String line)
	{
	 	line=line.replace("BSPO_passes_through","passes_through");
		line=line.replace("OBO_REL_located_in","RO_0001025");
		line=line.replace("RO_0002150","UBREL_0000001");
		line=line.replace("UBERON_has_muscle_insertion","has_muscle_insertion");
		line=line.replace("UBERON_in_lateral_side_of","BSPO_0000126");
		line=line.replace("UBERON_has_muscle_origin","has_muscle_origin");
		line=line.replace("UBERON_encloses","encloses");
		line=line.replace("UBERON_attaches_to","attaches_to");
		line=line.replace("UBERON_connects","connects");
		line=line.replace("UBERON_in_median_plane_of","BSPO_0000127");
		line=line.replace("UBERON_posteriorly_connected_to","posteriorly_connected_to");
		line=line.replace("UBERON_anteriorly_connected_to","anteriorly_connected_to");
		return(line);
	}
}
