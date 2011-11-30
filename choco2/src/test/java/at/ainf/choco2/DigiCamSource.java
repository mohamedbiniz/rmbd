/*
 * Copyright (c) 2009 Kostyantyn Shchekotykhin
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * http://www.gnu.org/licenses/gpl.txt
 */

package at.ainf.choco2;


import at.ainf.choco2.model.ConstraintTheory;
import at.ainf.diagnosis.model.UnsatisfiableFormulasException;
import at.ainf.diagnosis.model.SolverException;
import choco.cp.model.CPModel;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;

import java.util.LinkedList;
import java.util.List;

import static choco.Choco.*;

public class DigiCamSource {


    public ConstraintTheory fillTheory(Solver solver) throws UnsatisfiableFormulasException, SolverException {
        List<Constraint> changable = new LinkedList<Constraint>();
        List<Constraint> unchangable = new LinkedList<Constraint>();

        IntegerVariable highConnectivity = makeIntVar("high-connectivity", new int[]{0, 1});
        IntegerVariable usb = makeIntVar("usb", new int[]{0, 1});
        IntegerVariable fw = makeIntVar("firewire", new int[]{0, 1});
        IntegerVariable mobility = makeIntVar("mobility", new int[]{0, 1});
        IntegerVariable battery = makeIntVar("battery", new int[]{0, 1});
        IntegerVariable weight = makeIntVar("weight", 0, 3000);
        // 0 - fuji; 1 - sony; 2- canon; 3 - benq; 4 - samsung
        IntegerVariable manufacturer = makeIntVar("manufacturer", new int[]{0, 1, 2, 3, 4});
        IntegerVariable picturePrintout = makeIntVar("picture-printout", new int[]{0, 1});
        // resolution is in megapixels
        IntegerVariable resolution = makeIntVar("resolution", 0, 20);
        IntegerVariable srl = makeIntVar("SRL", new int[]{0, 1});
        Model md = new CPModel();
        // add product variables
        md.addVariables(highConnectivity, usb, fw, mobility, battery, weight,
                manufacturer, picturePrintout, resolution, srl);

        IntegerVariable lowPrice = makeIntVar("low-price", new int[]{0, 1});
        IntegerVariable price = makeIntVar("price", 0, 10000);
        IntegerVariable midPrice = makeIntVar("mid-price", new int[]{0, 1});
        IntegerVariable brandsOnly = makeIntVar("brands-only", new int[]{0, 1});

        md.addVariables(lowPrice, price, midPrice, brandsOnly);


        // r1) high-connectivity = true -> usb = true or firewire = true
        Constraint r1 = implies(eq(highConnectivity, 1), or(eq(usb, 1), eq(fw, 1)));

        // r2) mobility = true -> battery = true and weight < 200
        Constraint r2 = implies(eq(mobility, 1), and(eq(battery, 1), leq(weight, 200)));

        // r3) low-price = true -> price < 200
        Constraint r3 = implies(eq(lowPrice, 1), leq(price, 200));

        // r3') low-price = true -> price < 250
        Constraint r3bis = implies(eq(lowPrice, 1), leq(price, 250));

        // r4) mid-price = true -> price > 200 and price < 400
        Constraint r4 = implies(eq(midPrice, 1), and(geq(price, 200), leq(price, 400)));

        // r5) brands-only = true -> manufacturer = 'fuji' or manufacturer =
        // 'sony' or manufacturer = 'canon'
        Constraint r5 = implies(eq(brandsOnly, 1), or(eq(manufacturer, 0), eq(manufacturer, 1), eq(
                manufacturer, 2)));

        // r6) picture-printout = true -> resolution > 3MP
        Constraint r6 = implies(eq(picturePrintout, 1), geq(resolution, 3));

        // r7) low-price = true -> manufacturer = 'benq' or manufacturer =
        // 'samsung' (problem)
        Constraint r7 = implies(eq(lowPrice, 1), or(eq(manufacturer, 3), eq(manufacturer, 4)));

        // r7') low-price = true -> manufacturer = 'benq' AND manufacturer =
        // 'samsung' (problem)
        Constraint r7bis = implies(eq(lowPrice, 1), and(eq(manufacturer, 3), eq(manufacturer, 4)));

        // r8) low-price -> resolution < 2MP (problem)
        Constraint r8 = implies(eq(lowPrice, 1), leq(resolution, 2));

        // r9) picture-printout -> resolution > 4 MP or SLR = true
        Constraint r9 = implies(eq(picturePrintout, 1), or(eq(srl, 1), geq(resolution, 4)));

        // r10) low-price and picture-printout -> false
        Constraint r10 = implies(and(eq(lowPrice, 1), eq(picturePrintout, 1)), FALSE);

        // r11) brands-only = false -> manufacturer = 'benq' or manufacturer =
        // 'samsung' (problem)

        Constraint r11 = implies(eq(brandsOnly, 0), or(eq(manufacturer, 3), eq(manufacturer, 4)));

        changable.add(r1);
        changable.add(r2);
        changable.add(r3);
        changable.add(r3bis);
        changable.add(r4);
        changable.add(r5);
        changable.add(r6);
        changable.add(r7);
        changable.add(r7bis);
        changable.add(r8);
        changable.add(r9);
        changable.add(r10);
        changable.add(r11);

        //
        // 0 - fuji; 1 - sony; 2- canon; 3 - benq; 4 - samsung

        // examples (contributor 1):
        // e1: low-price: fuji, 5MP, 198.-
        Constraint[] e1 = new Constraint[]{eq(lowPrice, 1), eq(manufacturer, 0), eq(resolution, 5),
                eq(price, 198)};
        // e2: low-price : benq, 3 MP, 167.-
        Constraint[] e2 = new Constraint[]{eq(lowPrice, 1), eq(manufacturer, 3), eq(resolution, 3),
                eq(price, 167)};
        // e3: low-price : canon, 6 MP, 400.-
        Constraint[] e3 = new Constraint[]{eq(lowPrice, 1), eq(manufacturer, 2), eq(resolution, 6),
                eq(price, 200)};
        // e4: low-price : benq, 2 MP, 221.-
        Constraint[] e4 = new Constraint[]{eq(lowPrice, 1), eq(manufacturer, 3), eq(resolution, 2),
                eq(price, 221)};
        // e5: low-price : benq, 2 MP, 167.-
        Constraint[] e5 = new Constraint[]{eq(lowPrice, 1), eq(manufacturer, 3), eq(resolution, 2),
                eq(price, 167)};


        // e6: brands-only = false, mid-price: canon, 3MP, 300 MP
        Constraint[] e6 = new Constraint[]{eq(brandsOnly, 0), eq(lowPrice, 1),
                gt(resolution, 3), lt(price, 300)};

        Constraint[] union = new Constraint[]{and(e1), and(e2), and(e3), and(e4), or(e5)};
        unchangable.add(or(union));
        unchangable.add(and(e6));

        ConstraintTheory cth = new ConstraintTheory(solver, md);
        cth.setBackgroundFormulas(unchangable);
        cth.addConstraints(changable);
        return cth;
    }
}
