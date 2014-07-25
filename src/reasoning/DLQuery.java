
package reasoning;

import java.util.Set;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.ShortFormProvider;


public class DLQuery {
 
}


class DLQueryEngine {
    private OWLReasoner reasoner;
    private DLQueryParser parser;

    public DLQueryEngine(OWLReasoner reasoner, ShortFormProvider shortFormProvider) {
        this.reasoner = reasoner;
        OWLOntology rootOntology = reasoner.getRootOntology();
        parser = new DLQueryParser(rootOntology, shortFormProvider);
    }


    public String getSuperClasses(String classExpressionString, boolean direct)
            throws ParserException {
        if (classExpressionString.trim().length() == 0) {
            return "";
        }
        OWLClassExpression classExpression = parser
                .parseClassExpression(classExpressionString);
        NodeSet<OWLClass> superClasses = reasoner
                .getSuperClasses(classExpression, false);
        Set<OWLClass> clses = superClasses.getFlattened();
        String ancestorlist="";
        for (OWLClass cls : clses) {
            ancestorlist=ancestorlist+","+cls;
        }
        
        return(ancestorlist);
    }
}

class DLQueryParser {
    private OWLOntology rootOntology;
    private BidirectionalShortFormProvider bidiShortFormProvider;

    public DLQueryParser(OWLOntology rootOntology, ShortFormProvider shortFormProvider) {
        this.rootOntology = rootOntology;
        OWLOntologyManager manager = rootOntology.getOWLOntologyManager();
        Set<OWLOntology> importsClosure = rootOntology.getImportsClosure();
        // Create a bidirectional short form provider to do the actual mapping.
        // It will generate names using the input
        // short form provider.
        bidiShortFormProvider = new BidirectionalShortFormProviderAdapter(manager,
                importsClosure, shortFormProvider);
    }


    public OWLClassExpression parseClassExpression(String classExpressionString)
            throws ParserException {
        OWLDataFactory dataFactory = rootOntology.getOWLOntologyManager()
                .getOWLDataFactory();
        // Set up the real parser
        ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(
                dataFactory, classExpressionString);
        parser.setDefaultOntology(rootOntology);
        // Specify an entity checker that wil be used to check a class
        // expression contains the correct names.
        OWLEntityChecker entityChecker = new ShortFormEntityChecker(bidiShortFormProvider);
        parser.setOWLEntityChecker(entityChecker);
        // Do the actual parsing
        return parser.parseClassExpression();
    }
}

class DLQueryPrinter {
    private DLQueryEngine dlQueryEngine;
    

    public DLQueryPrinter(DLQueryEngine engine, ShortFormProvider shortFormProvider) {
    
        dlQueryEngine = engine;
    }


    public String askQuery(String classExpression) throws ParserException {
        
      
                
                String superClasses = dlQueryEngine.getSuperClasses(
                        classExpression, false);
                
                return(superClasses);
            } 
            
        
    }

 
