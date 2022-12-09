package metapath2vec;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Label;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.ResourceIterable;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.stream.Collectors;



/**
 * This is an example showing how you could expose Neo4j's full text indexes as
 * two procedures - one for updating indexes, and one for querying by label and
 * the lucene query language.
 */
public class GetMetaPathWalks {
    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/console.log`
    @Context
    public Log log;

    // custom
    @Context
    public Transaction transaction;

    /**
     * This procedure takes a ...
     *
     * @param pattern  The pattern
     * @return  A RelationshipTypes instance with the relations (incoming and outgoing) for a given node.
     */
    @Procedure(value = "metapath2vec.GetMetaPathWalks")
    // @Description("Get the different relationships going in and out of a node.")
    @Description("Generates meta-path walks.")
    public Stream<Walks> getMetaPathWalks(@Name("pattern") String pattern, @Name("length") Long walkLength) {

        Long walkLengthInt = walkLength;
        String[] patternArr = pattern.split(" ");
        List<String> walkLabels = new LinkedList<String>( Arrays.asList(patternArr) );

        ResourceIterator<Node> startNodes = transaction.findNodes(Label.label( walkLabels.get(0) ));
        walkLabels.remove(0);
        Walks wks = new Walks();
        wks.walks =  new ArrayList<>();
        while (startNodes.hasNext()) {
            Node firstNode = startNodes.next();
            wks.walks.add(getNodeWalks(firstNode, walkLengthInt - 1, walkLabels));
        }

        log.info(wks.toString());


        return Stream.of(wks);
    }

    /*
    @Procedure(value = "metapath2vec.TrainModel")
    @Description("Trains a Heterogeneous Skip-Gram model")
    public void trainModel(
            @Name("pattern") String pattern, 
            @Name("walks") Long walks, 
            @Name("length") Long walkLength, 
            @Name("embeddingDimension") Long embeddingDimension, 
            @Name("outputFile") String outputFile
            ) {
        
        // Get number of vertices

        // ArrayList[][] X = new ArrayList[
        
    }

    public void heterogeneousSkipGramUpdate(
            Variable<TFloat32> params, 
            int k, 
            Stream<Walks> metaPathWalks
            ) {

        Walks array = metaPathWalks.findFirst().get();
        for (int i = 0; i < metaPathWalks.count(); i++) {
            List<Long> v = array.walks.get(i);
            for(int j = Math.max(0, i - k); j < Math.min(i + k, v.size()); j++) {
                if(i != j) {
                    Long ct = array.walks.get(j);
                }
            }
        }
    }
    */

    /**
     * Performs
     *
     * @param Node author
     * @param int walkLength
     */
     private List<Node> getNodeWalks(Node startNode, Long walkLength, List<String> walkLabels) {
         
        List<Node> selected = new ArrayList<Node>();
        selected.add(startNode);

        if(walkLength < 1) {
            return selected;
        }
        // if trimming works, this condition is not necessary
        if(walkLabels.isEmpty()) {
            return selected;
        }
        
        Node node = startNode;
        Node previousNode = startNode;

        int j = 0;
        for(int i = 0; i < walkLength; i++) {

            j = i % walkLabels.size();


            String label = walkLabels.get(j);

            ResourceIterable<Relationship> relationships = node.getRelationships();
            List<Node> foundNodes = new ArrayList<Node> ();
            // iterate relations
            for (Relationship rel : relationships) {
                Node otherNode = rel.getOtherNode(node);
                // see if matches the first label in the pattern
                // and not the previous node in the walk
                if(otherNode.hasLabel(Label.label(label))) {
                    if(!previousNode.equals(otherNode))
                        foundNodes.add(otherNode);
                }
            }

            // if no node found of that label then it is the end
            // but the walks should be trimmed to the last valid label
            if(foundNodes.isEmpty())
                break;

            // Intialize random library
            Random rand = new Random();

            // Select a random node uniformly from the list of found nodes
            Node chosenNode = foundNodes.get(rand.nextInt(foundNodes.size()));

            // And add it to the selected
            selected.add(chosenNode);

            // Iterate
            previousNode = node;
            node = chosenNode;

        }

        return selected;
    }

    public class Walks {
        public List<List<Node>> walks;

        public String toString() {
            // TODO: Convert this to StringBuilder
            String str = new String(); 

            for(List<Node> walk : walks) {
                String comp = walk.stream().map(x -> String.valueOf(x.getId())).collect(Collectors.joining(" "));
                str += comp + "\n";
            }

            return str;
        }
    }
}
