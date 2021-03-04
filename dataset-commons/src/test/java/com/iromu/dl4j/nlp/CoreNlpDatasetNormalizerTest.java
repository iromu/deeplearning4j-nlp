package com.iromu.dl4j.nlp;


import com.iromu.dl.nlp.CoreNlpDatasetNormalizer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CoreNlpDatasetNormalizerTest {

    public static final String text = "Joe Smith was born in California. " +
            "In 2017, he went to Paris, France in the summer. " +
            "His flight left at 3:00pm on July 10th, 2017. " +
            "After eating some escargot for the first time, Joe said, \"That was delicious!\" " +
            "He sent a postcard to his sister Jane Smith. " +
            "After hearing about Joe's trip, Jane decided she might go to France one day.";

    public static final String expected = "Joe Smith be bear in California " +
            "in he go to Paris France in the summer " +
            "he flight leave at pm on July th " +
            "after eat some escargot for the first time Joe say that be delicious " +
            "he send a postcard to he sister Jane Smith " +
            "after hear about Joe s trip Jane decide she might go to France one day";

    public static final String text2 = "From: mmadsen@bonnie.ics.uci.edu (Matt Madsen)\n" +
            "Subject: Re: Please Recommend 3D Graphics Library For Mac.\n" +
            "\n" +
            "Robert G. Carpenter writes:\n" +
            "\n" +
            ">Hi Netters,\n" +
            ">\n" +
            ">I'm building a CAD package and need a 3D graphics library that can handle\n" +
            ">some rudimentry tasks, such as hidden line removal, shading, animation, etc.\n" +
            ">\n" +
            ">Can you please offer some recommendations?\n" +
            ">\n" +
            ">I'll also need contact info (name, address, email...) if you can find it.\n" +
            ">\n" +
            ">Thanks\n" +
            ">\n" +
            ">(Please Post Your Responses, in case others have same need)\n" +
            ">\n" +
            ">Bob Carpenter\n" +
            ">\n" +
            "\n" +
            "I too would like a 3D graphics library!  How much do C libraries cost\n" +
            "anyway?  Can you get the tools used by, say, RenderMan, and can you get\n" +
            "them at a reasonable cost?\n" +
            "\n" +
            "Sorry that I don't have any answers, just questions...\n" +
            "\n" +
            "Matt Madsen\n" +
            "mmadsen@ics.uci.edu\n" +
            "\n";

    public static final String expected2 = "from mmadsen@bonnieicsuciedu Matt Madsen " +
            "subject re please recommend d graphic Library for Mac " +
            "Robert G Carpenter write " +
            "hi netter " +
            "I be build a cad package and need a d graphic library that can handle " +
            "some rudimentry task such as hidden line removal shading animation etc " +
            "can you please offer some recommendation " +
            "I will also need contact info name address email if you can find it " +
            "thanks " +
            "please post you response in case other have same need " +
            "Bob Carpenter " +
            "I too would like a d graphic library how much do c library cost " +
            "anyway can you get the tool use by say RenderMan and can you get " +
            "they at a reasonable cost " +
            "sorry that I do not have any answer just question " +
            "Matt Madsen " +
            "mmadsen@icsuciedu";
    @Test
    public void process() {
        String actual = new CoreNlpDatasetNormalizer().process(text);
        assertEquals(expected, actual);
    }

    @Test
    public void process2() {
        String actual = new CoreNlpDatasetNormalizer().process(text2);
        assertEquals(expected2, actual);
    }
}
