package uk.me.berndporr.iirj;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.File;

import uk.me.berndporr.iirj.Butterworth;

import org.junit.Assert;
import org.junit.Test;

// Various impulse responses written out to files so that you can plot them
public class ButterworthTest {

	static String prefix="target/surefire-reports/butterworth/";

	static double fs = 250;
	static double fc = 10;
	static int order = 6;
	static int nSteps = 10000;

	void createDir() throws Exception {
		File dir = new File(prefix);
		dir.mkdirs();
	}		

	@Test
	public void lowPassTest() throws Exception {

		Butterworth butterworth = new Butterworth();
		butterworth.lowPass(order, fs, fc);

		createDir();
		FileOutputStream os = new FileOutputStream(prefix+"lp.txt");
		PrintStream bp = new PrintStream(os);

		// let's do an impulse response
		double v = 0;
		for (int i = 0; i < nSteps; i++) {
			v = 0;
			if (i == 10)
				v = 1;
			v = butterworth.filter(v);
			bp.println("" + v);
		}
		System.out.println("Lowpass filter output = "+v);
		Assert.assertTrue(v < 1E-80);
		Assert.assertTrue(v != 0.0);
		Assert.assertTrue(v != Double.NaN);

		os.close();
	}

	@Test
	public void bandPassTest() throws Exception {
		Butterworth butterworth = new Butterworth();
		butterworth.bandPass(order, fs, fc, fc/4);

		createDir();
		FileOutputStream os = new FileOutputStream(prefix+"bp.txt");
		PrintStream bp = new PrintStream(os);

		// let's do an impulse response
		double v = 0;
		for (int i = 0; i < nSteps; i++) {
			v = 0;
			if (i == 10)
				v = 1;
			v = butterworth.filter(v);
			bp.println("" + v);
		}
		System.out.println("Bandpass filter output = "+v);
		Assert.assertTrue(v < 1E-10);
		Assert.assertTrue(v != 0.0);
		Assert.assertTrue(v != Double.NaN);

		os.close();
	}

	@Test
	public void bandStopTest() throws Exception {
		Butterworth butterworth = new Butterworth();
		butterworth.bandStop(order, fs, fc, fc/4);

		createDir();
		FileOutputStream os = new FileOutputStream(prefix+"bs.txt");
		PrintStream bp = new PrintStream(os);

		// let's do an impulse response
		double v = 0;
		for (int i = 0; i < nSteps; i++) {
			v = 0;
			if (i == 10)
				v = 1;
			v = butterworth.filter(v);
			bp.println("" + v);
		}
		System.out.println("Bandstop filter output = "+v);
		Assert.assertTrue(v < 1E-10);
		Assert.assertTrue(v != 0.0);
		Assert.assertTrue(v != Double.NaN);

		os.close();
	}

	@Test
	public void highPassTest() throws Exception {
		Butterworth butterworth = new Butterworth();
		butterworth.highPass(order, fs, fc);

		createDir();
		FileOutputStream os = new FileOutputStream(prefix+"hp.txt");
		PrintStream bp = new PrintStream(os);

		// let's do an impulse response
		double v = 0;
		for (int i = 0; i < nSteps; i++) {
			v = 0;
			if (i == 10)
				v = 1;
			v = butterworth.filter(v);
			bp.println("" + v);
		}
		System.out.println("Highpass filter output = "+v);
		Assert.assertTrue(v < 1E-80);
		Assert.assertTrue(v != 0.0);
		Assert.assertTrue(v != Double.NaN);

		os.close();
	}

	public void main(String args[]) {
		try {
			lowPassTest();
			highPassTest();
			bandPassTest();
			bandStopTest();
		} catch (Exception e) {
			Assert.fail("Exception while executing the filtering op:"+e.getMessage());
		}
	}
}
