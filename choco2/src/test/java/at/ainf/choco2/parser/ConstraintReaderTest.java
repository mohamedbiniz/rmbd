/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.choco2.parser;

import at.ainf.choco2.model.ConstraintTheory;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

public class ConstraintReaderTest {

    private static Logger logger = Logger.getLogger(ConstraintReaderTest.class.getName());

    @BeforeClass
    public static void setUp() {
        String conf = ClassLoader.getSystemResource("choco2-log4j.properties").getFile();
        PropertyConfigurator.configure(conf);
    }

    @Test
    public void testReader() throws Exception {
        String name = ClassLoader.getSystemResource("test.cons").getFile();
        ConstraintReader reader = new ConstraintReader();
        ConstraintTheory source =
                reader.getConstraints(new File(name), new CPSolver(), new CPModel());
        logger.info(source);
        //reader.getConstraints("(c1 = 5) -> ((c2 = 6) && (c3 < 6))", pb);
    }
}
