package com.iromu.dl4j.nlp;


import com.iromu.dl.nlp.OpenNlpDatasetNormalizer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OpenNlpDatasetNormalizerTest {

    public static final String text = "Joe Smith was born in California. " +
            "In 2017, he went to Paris, France in the summer. " +
            "His flight left at 3:00pm on July 10th, 2017. " +
            "After eating some escargot for the first time, Joe said, \"That was delicious!\" " +
            "He sent a postcard to his sister Jane Smith. " +
            "After hearing about Joe's trip, Jane decided she might go to France one day.";

    public static final String expected = "be bear in " +
            "in he go to in the " +
            "his leave at on " +
            "after eat some escargot for the first say that be delicious " +
            "he send a postcard to his sister " +
            "after hear about trip decide she might go to one";

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

    public static final String expected2 = "from " +
            "please for write " +
            "build a cad package and need a graphic that can handle " +
            "task as shade " +
            "you please offer some recommendation " +
            "also need info name email you can find it " +
            "please your response in other have same " +
            "too will like a graphic how do library " +
            "anyway can you get the tool use by say and can you get " +
            "they at a reasonable " +
            "sorry do have any answer just question";

    @Test
    public void process() {
        String actual = new OpenNlpDatasetNormalizer().process(text);
        assertEquals(expected, actual);
    }

    @Test
    public void process2() {
        String actual = new OpenNlpDatasetNormalizer().process(text2);
        assertEquals(expected2, actual);
    }
}
