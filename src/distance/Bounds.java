package distance;

import java.util.*;

public class Bounds {
	public static double lbKeogh(double q[], double u[], double l[], double bsf) {
		double lb = 0.0;
		for (int i = 0; i < q.length && lb <= bsf; i++) {
			double qi = q[i];
			if (qi>u[i]) {
				lb += dist(qi, u[i]);
			}
			else if (qi < l[i]) {
				lb += dist(qi,  l[i]);
			}
		}
		return lb;
	}
	
	public static double lbEnhanced(double q[], double t[], double u[], double l[], int k, int w, double bsf) {
		double lb = 0.0;
		
		if (k > q.length/2) k = q.length/2;
		
		if (k > 0) {
			double q0 = q[0];
			double qe0 = q[q.length-1];
			double t0 = t[0];
			double te0 = t[t.length-1];
			
			lb = dist(q0, t0)+dist(qe0,te0);
			
			for (int i = 1; i < k && lb <= bsf; i++) {
				double mind;
				double minde;
				int startj;
				double ti = t[i];
				double qi = q[i];
				double tei = t[t.length - 1 - i];
				double qei = q[q.length - 1 - i];
	
				if (i <= w) {
					// starting from the left of the matrix and going to the top
					mind = Math.min(dist(q0, ti), Math.min(dist(t0, qi), dist(qi, ti)));
					minde = Math.min(dist(qe0, tei), Math.min(dist(te0, qei), dist(tei, qei)));
					startj = 1;
				} else {
					mind = dist(qi, ti);
					minde = dist(tei, qei);
					startj = i - w;
				}
	
				for (int j = startj; j < i; j++) {
					double d = Math.min(dist(qi, t[j]), dist(ti, q[j]));
					if (d < mind)
						mind = d;
	
					double de = Math.min(dist(qei, t[t.length - 1 - j]), dist(tei, q[q.length - 1 - j]));
					if (de < minde)
						minde = de;
				}
	
				lb += mind + minde;
			}
		}
		
		for (int i = k; i < q.length-k && lb <= bsf; i++) {
			double qi = q[i];
			if (qi>u[i]) {
				lb += dist(qi, u[i]);
			}
			else if (qi < l[i]) {
				lb += dist(qi,  l[i]);
			}
		}
		
		return lb;
	}

	// do the leading and trailing bands without bridging the intervening rows or columns.
	// Can be coupled with an auxiliary function that covers the bridge to enhance any standard lower bound
	// RETURNS the lower bound SQUARED
	public static double lbEnhance(double q[], double t[], int k, int w, double bsf) {
		if (k > q.length/2) k = q.length/2;
		
		double q0 = q[0];
		double qe0 = q[q.length-1];
		double t0 = t[0];
		double te0 = t[t.length-1];
		
		double lb = dist(q0, t0)+dist(qe0,te0);
		
		if (lb > bsf) return lb;
		
		for (int i = 1; i < k && lb <= bsf; i++) {
			double mind;
			double minde;
			int startj;
			double ti = t[i];
			double qi = q[i];
			double tei = t[t.length - 1 - i];
			double qei = q[q.length - 1 - i];

			if (i <= w) {
				// starting from the left of the matrix and going to the top
				mind = Math.min(dist(q0, ti), Math.min(dist(t0, qi), dist(qi, ti)));
				minde = Math.min(dist(qe0, tei), Math.min(dist(te0, qei), dist(tei, qei)));
				startj = 1;
			} else {
				mind = dist(qi, ti);
				minde = dist(tei, qei);
				startj = i - w;
			}

			for (int j = startj; j < i; j++) {
				double d = Math.min(dist(qi, t[j]), dist(ti, q[j]));
				if (d < mind)
					mind = d;

				double de = Math.min(dist(qei, t[t.length - 1 - j]), dist(tei, q[q.length - 1 - j]));
				if (de < minde)
					minde = de;
			}

			lb += mind + minde;
		}
		
		return lb;
	}
	
	public static double lbEnhancePlus(double q[], double t[], int k, int w, double bsf) {
		double lb;
		
		if (2 * k > q.length) k = q.length/2;
				
		int istart;
		double q0 = q[0];
		double t0 = t[0];
		double qe0 = q[q.length-1];
		double te0 = t[t.length-1];
		
		if (k >= 3) {
			// the path through the first, second and third alignments
			// the seven possible paths are
			//	00, 01, 02
			//	00, 01, 12
			//	00, 11, 12
			//	00, 11, 22
			//	00, 11, 21
			//	00, 10, 21
			//	00, 10, 20

			double q1 = q[1];
			double t1 = t[1];
			double t2 = t[2];
			double q2 = q[2];
			
			double d01 = dist(q0, t1);
			double d11 = dist(q1, t1);
			double d10 = dist(q1,t0);
			
			lb = dist(q0, t0)+
					Math.min(
							d11 + dist(q2, t2),
							Math.min(
									Math.min(d01 + dist(q0, t2),
											 Math.min(d01, d11) + dist(q1, t2)),
									Math.min(d10 + dist(q2, t0),
											 Math.min(d10, d11) + dist(q2, t1))));
			
			if (lb > bsf) return lb;
	
			// the path through the last alignments
			q1 = q[q.length-2];
			t1 = t[t.length-2];
			t2 = t[t.length-3];
			q2 = q[q.length-3];
			
			d01 = dist(qe0, t1);
			d11 = dist(q1, t1);
			d10 = dist(q1,te0);
			
			lb += dist(qe0, te0)+
					Math.min(
							d11 + dist(q2, t2),
							Math.min(
									Math.min(d01 + dist(qe0, t2),
											 Math.min(d01, d11) + dist(q1, t2)),
									Math.min(d10 + dist(q2, te0),
											 Math.min(d10, d11) + dist(q2, t1))));
			
			istart = 3;
		}
		else {
			istart = 1;
			lb = dist(q[0], t[0]) + dist(q[q.length-1], t[t.length-1]);
		}

		for (int i = istart; i < k && lb <= bsf; i++) {
			double mind;
			double minde;
			int startj;
			double ti = t[i];
			double qi = q[i];
			double tei = t[t.length - 1 - i];
			double qei = q[q.length - 1 - i];

			if (i <= w) {
				// starting from the left of the matrix and going to the top
				mind = Math.min(dist(q0, ti), Math.min(dist(t0, qi), dist(qi, ti)));
				minde = Math.min(dist(qe0, tei), Math.min(dist(te0, qei), dist(tei, qei)));
				startj = 1;
			} else {
				mind = dist(qi, ti);
				minde = dist(tei, qei);
				startj = i - w;
			}

			for (int j = startj; j < i; j++) {
				double d = Math.min(dist(qi, t[j]), dist(ti, q[j]));
				if (d < mind)
					mind = d;

				double de = Math.min(dist(qei, t[t.length - 1 - j]), dist(tei, q[q.length - 1 - j]));
				if (de < minde)
					minde = de;
			}

			lb += mind + minde;
		}
		
		return lb;
	}


	public static double lbImproved(double q[], double t[], double ut[], double lt[], int w, double bsf) {
		double lb = 0.0;

		if (proj == null) {
			proj = new double[defaultAllocSize];
		}
		else if (proj.length < q.length) {
			proj = new double[q.length];
		}


		// do standard lbKeogh but collect projection along the way
		for (int i = 0; i < q.length && lb <= bsf; i++) {
			double qi = q[i];
			if (qi>ut[i]) {
				lb += dist(qi, ut[i]);
				proj[i]= ut[i];
			}
			else if (qi < lt[i]) {
				lb += dist(qi,  lt[i]);
				proj[i] = lt[i];
			}
			else {
				proj[i] = qi;
			}
		}
		
		if (lb > bsf) return lb;

		if (up == null) {
			  up = new double[defaultAllocSize];
			  lp = new double[defaultAllocSize];
		}
		else if (up.length < q.length) {
		  up = new double[q.length];
		  lp = new double[q.length];
		}

		lemireGetEnvelopes(proj, w, up, lp);
		
		lb += lbKeogh(t, up, lp, bsf);
		
		return lb;
	}

	public static double lbPetitjeanNoEnhance(double q[], double uq[], double lq[], double t[], double ut[], double lt[], int w, double bsf, boolean doKeogh) {
		double lb = 0.0;

		if (proj == null) {
			proj = new double[defaultAllocSize];
		}
		else if (proj.length < q.length) {
			proj = new double[q.length];
		}
		
		if (doKeogh) {
			// do standard lbKeogh but collect projection along the way
			for (int i = 0; i < q.length && lb <= bsf; i++) {
				double qi = q[i];
				if (qi>ut[i]) {
					lb += dist(qi, ut[i]);
					proj[i]= ut[i];
				}
				else if (qi < lt[i]) {
					lb += dist(qi,  lt[i]);
					proj[i] = lt[i];
				}
				else {
					proj[i] = qi;
				}
			}
			
			if (lb > bsf) return lb;
		}
		else {
			//  collect projection 
			for (int i = 0; i < q.length && lb <= bsf; i++) {
				double qi = q[i];
				if (qi>ut[i]) {
					proj[i]= ut[i];
				}
				else if (qi < lt[i]) {
					proj[i] = lt[i];
				}
				else {
					proj[i] = qi;
				}
			}
		}

		if (up == null) {
			  up = new double[defaultAllocSize];
			  lp = new double[defaultAllocSize];
		}
		else if (up.length < q.length) {
		  up = new double[q.length];
		  lp = new double[q.length];
		}

		lemireGetEnvelopes(proj, w, up, lp);
		
		for (int i = 0; i < t.length && lb <= bsf; i++) {
			double ti = t[i];
			double upi = up[i];
			
			if (ti>upi) {
				double uqi = uq[i];
				if (upi > uqi) {
					lb += dist(ti, uqi) - dist(upi, uqi);
				}
				else {
					lb += dist(ti, upi);
				}
			}
			else {
				double lpi = lp[i];
				if (ti < lpi) {
					double lqi = lq[i];
					if (lpi < lqi) {
						lb += dist(ti, lqi) - dist(lpi, lqi);
					}
					else {
						lb += dist(ti,  lpi);
					}
				}
			}
		}
		return lb;
	}

	public static double lbPetitjean(double q[], double uq[], double lq[], double t[], double ut[], double lt[], int w, double bsf) {
		double lb = 0;
		int istart = 0;
		
		if (w >= 1 && q.length >= 6) {
			// the path through the first, second and third alignments
			// the seven possible paths are
			//	00, 01, 02
			//	00, 01, 12
			//	00, 11, 12
			//	00, 11, 22
			//	00, 11, 21
			//	00, 10, 21
			//	00, 10, 20
			double q0 = q[0];
			double t0 = t[0];
			double qe0 = q[q.length-1];
			double te0 = t[t.length-1];
			double q1 = q[1];
			double t1 = t[1];
			double t2 = t[2];
			double q2 = q[2];
			
			double d01 = dist(q0, t1);
			double d11 = dist(q1, t1);
			double d10 = dist(q1,t0);
			
			if (w == 1) {
				lb = dist(q0, t0)+
						Math.min(
								d11 + dist(q2, t2),
								Math.min(
										Math.min(d01, d11) + dist(q1, t2),
										Math.min(d10, d11) + dist(q2, t1)));
			}
			else {
				lb = dist(q0, t0)+
					Math.min(
							d11 + dist(q2, t2),
							Math.min(
									Math.min(d01 + dist(q0, t2),
											 Math.min(d01, d11) + dist(q1, t2)),
									Math.min(d10 + dist(q2, t0),
											 Math.min(d10, d11) + dist(q2, t1))));
			}
			
			if (lb > bsf) return lb;
	
			// the path through the last alignments
			q1 = q[q.length-2];
			t1 = t[t.length-2];
			t2 = t[t.length-3];
			q2 = q[q.length-3];
			
			d01 = dist(qe0, t1);
			d11 = dist(q1, t1);
			d10 = dist(q1,te0);
			
			if (w == 1) {
				lb += dist(qe0, te0)+
						Math.min(
								d11 + dist(q2, t2),
								Math.min(
										Math.min(d01, d11) + dist(q1, t2),
										Math.min(d10, d11) + dist(q2, t1)));
			}
			else {
				lb += dist(qe0, te0)+
						Math.min(
								d11 + dist(q2, t2),
								Math.min(
										Math.min(d01 + dist(qe0, t2),
												 Math.min(d01, d11) + dist(q1, t2)),
										Math.min(d10 + dist(q2, te0),
												 Math.min(d10, d11) + dist(q2, t1))));
			}

			if (lb > bsf) return lb;
			
			istart = 3;
		}
		
		if (proj == null) {
			proj = new double[defaultAllocSize];
		}
		else if (proj.length < q.length) {
			proj = new double[q.length];
		}
		
		// do standard lbKeogh but collect projection along the way
		for (int i = istart; i < q.length-istart && lb <= bsf; i++) {
			double qi = q[i];
			if (qi>ut[i]) {
				lb += dist(qi, ut[i]);
				proj[i]= ut[i];
			}
			else if (qi < lt[i]) {
				lb += dist(qi,  lt[i]);
				proj[i] = lt[i];
			}
			else {
				proj[i] = qi;
			}
		}
		
		if (lb > bsf) return lb;
		
		// fill in the projection for the region that was enhanced.  Note, none of these points in q have been considered for alignment with t[k, ..., q.length-k]
		for (int i = 0; i < istart; i++) {
			proj[i] = q[i];
			proj[q.length-i-1] = q[q.length-i-1];
		}

		if (up == null) {
			  up = new double[Math.max(defaultAllocSize,q.length)];
			  lp = new double[Math.max(defaultAllocSize,q.length)];
		}
		else if (up.length < q.length) {
		  up = new double[q.length];
		  lp = new double[q.length];
		}

		lemireGetEnvelopes(proj, w, up, lp);
		
		for (int i = istart; i < t.length-istart && lb <= bsf; i++) {
			double ti = t[i];
			if (ti>up[i]) {
				if (up[i] > uq[i]) {
					lb += dist(ti, uq[i]) - dist(up[i], uq[i]);
				}
				else {
					lb += dist(ti, up[i]);
				}
			}
			else if (ti < lp[i]) {
				if (lp[i] < lq[i]) {
					lb += dist(ti, lq[i]) - dist(lp[i], lq[i]);
				}
				else {
					lb += dist(ti,  lp[i]);
				}
			}
		}
		
		return lb;
	}
	

	public static double lbWebb(double q[], double uq[], double lq[], double t[], double ut[], double lt[], double lut[], double ult[], int window, double bsf) {
		double lb = 0;
		int istart = 0;
		
		if (window >= 1 && q.length >= 6) {
			// the path through the first, second and third alignments
			// the seven possible paths are
			//	00, 01, 02
			//	00, 01, 12
			//	00, 11, 12
			//	00, 11, 22
			//	00, 11, 21
			//	00, 10, 21
			//	00, 10, 20
			double q0 = q[0];
			double t0 = t[0];
			double qe0 = q[q.length-1];
			double te0 = t[t.length-1];
			double q1 = q[1];
			double t1 = t[1];
			double t2 = t[2];
			double q2 = q[2];
			
			double d01 = dist(q0, t1);
			double d11 = dist(q1, t1);
			double d10 = dist(q1,t0);
			
			if (window == 1) {
				lb = dist(q0, t0)+
						Math.min(
								d11 + dist(q2, t2),
								Math.min(
										Math.min(d01, d11) + dist(q1, t2),
										Math.min(d10, d11) + dist(q2, t1)));
			}
			else {
				lb = dist(q0, t0)+
					Math.min(
							d11 + dist(q2, t2),
							Math.min(
									Math.min(d01 + dist(q0, t2),
											 Math.min(d01, d11) + dist(q1, t2)),
									Math.min(d10 + dist(q2, t0),
											 Math.min(d10, d11) + dist(q2, t1))));
			}
			
			if (lb > bsf) return lb;
	
			// the path through the last alignments
			q1 = q[q.length-2];
			t1 = t[t.length-2];
			t2 = t[t.length-3];
			q2 = q[q.length-3];
			
			d01 = dist(qe0, t1);
			d11 = dist(q1, t1);
			d10 = dist(q1,te0);
			
			if (window == 1) {
				lb += dist(qe0, te0)+
						Math.min(
								d11 + dist(q2, t2),
								Math.min(
										Math.min(d01, d11) + dist(q1, t2),
										Math.min(d10, d11) + dist(q2, t1)));
			}
			else {
				lb += dist(qe0, te0)+
						Math.min(
								d11 + dist(q2, t2),
								Math.min(
										Math.min(d01 + dist(qe0, t2),
												 Math.min(d01, d11) + dist(q1, t2)),
										Math.min(d10 + dist(q2, te0),
												 Math.min(d10, d11) + dist(q2, t1))));
			}

			if (lb > bsf) return lb;
			
			istart = 3;
		}
		
		int freeCount = window;

	 	// isFree will be true if no q within the window has been included in lbKeogh
		if (isFree == null) {
			isFree = new boolean[Math.max(q.length,defaultAllocSize)];
		}
		else if (isFree.length < q.length) {
			isFree = new boolean[q.length];
		}
		else {
			 java.util.Arrays.fill(isFree, false);
		}
		
		int qEnd = q.length-istart;

		// lbKeogh
		for (int i = istart; i < qEnd && lb <= bsf; i++) {
				double qi = q[i];
				if (qi>ut[i]) {
					lb += dist(qi, ut[i]);
					freeCount = 0;
					//if (i>= window) isFree[i-window] = false;
				}
				else if (qi < lt[i]) {
					lb += dist(qi,  lt[i]);
					freeCount = 0;
					//if (i>= window) isFree[i-window] = false;
				}
				else {
					freeCount++;
		
					// update freeCount
					if (freeCount > 2*window) isFree[i-window] = true;
					//else if (i>= window) isFree[i-window] = false;
				}

		}
		
		for (int i = qEnd - freeCount + window; i < qEnd; i++) {
			isFree[i] = true;
		}

	
		// now add distance from t to q
		for (int i = istart; i < qEnd && lb <= bsf; i++) {
			if (isFree[i]){
				if (t[i] > uq[i]) {
					lb += dist(t[i], uq[i]);
				}
				else {
					if (t[i] < lq[i]) {
						lb += dist(t[i], lq[i]);
					}
				} 
			} else {
				if (ult[i] >= uq[i]) {
					if (t[i]>ult[i]) {
						lb += dist(t[i], uq[i]) - dist(ult[i], uq[i]);
					}
				}
				else {
					if (t[i] < lut[i]) {
						if (lut[i] <= lq[i]) {
							lb += dist(t[i],  lq[i]) - dist(lut[i], lq[i]);
						}
					}
				}
			}
		}
		
		return lb;
	}

	public static double enhancedLBWebb(double q[], double uq[], double lq[], double t[], double ut[], double lt[], double lut[], double ult[], int k, int window, double bsf) {
		double lb = 0.0;
		
		if (k > q.length/2) k = q.length/2;
		
		if (k > 0) {
			double q0 = q[0];
			double qe0 = q[q.length-1];
			double t0 = t[0];
			double te0 = t[t.length-1];
			
			lb = dist(q0, t0)+dist(qe0,te0);
			
			for (int i = 1; i < k && lb <= bsf; i++) {
				double mind;
				double minde;
				int startj;
				double ti = t[i];
				double qi = q[i];
				double tei = t[t.length - 1 - i];
				double qei = q[q.length - 1 - i];
	
				if (i <= window) {
					// starting from the left of the matrix and going to the top
					mind = Math.min(dist(q0, ti), Math.min(dist(t0, qi), dist(qi, ti)));
					minde = Math.min(dist(qe0, tei), Math.min(dist(te0, qei), dist(tei, qei)));
					startj = 1;
				} else {
					mind = dist(qi, ti);
					minde = dist(tei, qei);
					startj = i - window;
				}
	
				for (int j = startj; j < i; j++) {
					double d = Math.min(dist(qi, t[j]), dist(ti, q[j]));
					if (d < mind)
						mind = d;
	
					double de = Math.min(dist(qei, t[t.length - 1 - j]), dist(tei, q[q.length - 1 - j]));
					if (de < minde)
						minde = de;
				}
	
				lb += mind + minde;
			}
		}
		
		if (lb > bsf) return lb;
		
		int istart = k;
		
		int freeCount = window;

	 	// isFree will be true if no q within the window has been included in lbKeogh
		if (isFree == null) {
			isFree = new boolean[Math.max(q.length,defaultAllocSize)];
		}
		else if (isFree.length < q.length) {
			isFree = new boolean[q.length];
		}
		else {
			 java.util.Arrays.fill(isFree, false);
		}
		
		int qEnd = q.length-istart;

		// lbKeogh
		for (int i = istart; i < qEnd && lb <= bsf; i++) {
				double qi = q[i];
				if (qi>ut[i]) {
					lb += dist(qi, ut[i]);
					freeCount = 0;
					//if (i>= window) isFree[i-window] = false;
				}
				else if (qi < lt[i]) {
					lb += dist(qi,  lt[i]);
					freeCount = 0;
					//if (i>= window) isFree[i-window] = false;
				}
				else {
					freeCount++;
		
					// update freeCount
					if (freeCount > 2*window) isFree[i-window] = true;
					//else if (i>= window) isFree[i-window] = false;
				}

		}
		
		for (int i = qEnd - freeCount + window; i < qEnd; i++) {
			isFree[i] = true;
		}

	
		// now add distance from t to q
		for (int i = istart; i < qEnd && lb <= bsf; i++) {
			if (isFree[i]){
				if (t[i] > uq[i]) {
					lb += dist(t[i], uq[i]);
				}
				else {
					if (t[i] < lq[i]) {
						lb += dist(t[i], lq[i]);
					}
				} 
			} else {
				if (ult[i] >= uq[i]) {
					if (t[i]>ult[i]) {
						lb += dist(t[i], uq[i]) - dist(ult[i], uq[i]);
					}
				}
				else {
					if (t[i] < lut[i]) {
						if (lut[i] <= lq[i]) {
							lb += dist(t[i],  lq[i]) - dist(lut[i], lq[i]);
						}
					}
				}
			}
		}
		
		return lb;
	}

	// Fast approximation to lbWebb using only free zone
	public static double lbBWebbOnlyFreeZone(double q[], double uq[], double lq[], double t[], double ut[], double lt[], int window, double bsf) {
		double lb = 0.0;
		int freeCount = window;
		
	 	// isFree will be true if no q within the window has been included in lbKeogh
		if (isFree == null) {
			isFree = new boolean[defaultAllocSize];
		}
		else if (isFree.length < q.length) {
			isFree = new boolean[q.length];
		}
		
		// lbKeogh
		for (int i = 0; i < q.length && lb <= bsf; i++) {
			double qi = q[i];
			if (qi>ut[i]) {
				lb += dist(qi, ut[i]);
				freeCount = 0;
			}
			else if (qi < lt[i]) {
				lb += dist(qi,  lt[i]);
				freeCount = 0;
			}
			else {
				freeCount++;
			}

			// update freeCount
			if (freeCount > 2*window) isFree[i-window] = true;
			else if (i>= window) isFree[i-window] = false;
		}
		
		// fix isFree for the end
		for (int i = Math.max(0, q.length - window); i < Math.min(q.length - freeCount + window, q.length); i++) {
			isFree[i] = false;
		}
		
		for (int i = q.length - freeCount + window; i < q.length; i++) {
			isFree[i] = true;
		}
		
		// now add distance from t to q
		for (int i = 0; i < t.length && lb <= bsf; i++) {
			double ti = t[i];
			if (isFree[i]){
				if (ti < lq[i]) {
					lb += dist(ti, lq[i]);
				}
				else if (ti > uq[i]) {
					lb += dist(ti, uq[i]);
				}
			}
		}
		
		return lb;
	}

	// lb webb without tracking whether all q within the window have been within t's envelope
	public static double fastLBWNoFreeZone(double q[], double uq[], double lq[], double t[], double ut[], double lt[], double lut[], double ult[], int window, double bsf, boolean doKeogh) {
		double lb = 0.0;
		
		if (doKeogh) {
			// lbKeogh
			for (int i = 0; i < q.length && lb <= bsf; i++) {
				double qi = q[i];
				if (qi>ut[i]) {
					lb += dist(qi, ut[i]);
				}
				else if (qi < lt[i]) {
					lb += dist(qi,  lt[i]);
				}
			}
		}
		
		// now add distance from t to q
		for (int i = 0; i < t.length && lb <= bsf; i++) {
			double ti = t[i];
			if (ti>ult[i] && ult[i] >= uq[i]) {
				lb += dist(ti, uq[i]) - dist(ult[i], uq[i]);
			}
			else if (ti < lut[i] && lut[i] <= lq[i]) {
				lb += dist(ti,  lq[i]) - dist(lut[i], lq[i]);
			}
		}
		
		return lb;
	}
	
	// lb webb without tracking whether all q within the window have been within t's envelope
	public static double lbWNoFreeZoneAux(double q[], double uq[], double lq[], double t[], double ut[], double lt[], double lut[], double ult[], int k, int window, double lb, double bsf) {
		if (2 * k > q.length) k = q.length/2;
		
		// lbKeogh
		for (int i = k; i < q.length-k && lb <= bsf; i++) {
			double qi = q[i];
			if (qi>ut[i]) {
				lb += dist(qi, ut[i]);
			}
			else if (qi < lt[i]) {
				lb += dist(qi,  lt[i]);
			}
		}
		
		// now add distance from t to q
		for (int i = k; i < t.length-k && lb <= bsf; i++) {
			double ti = t[i];
			if (ti>ult[i] && ult[i] >= uq[i]) {
				lb += dist(ti, uq[i]) - dist(ult[i], uq[i]);
			}
			else if (ti < lut[i] && lut[i] <= lq[i]) {
				lb += dist(ti,  lq[i]) - dist(lut[i], lq[i]);
			}
		}
		
		return lb;
	}
	

	public static void simpleGetEnvelopes(double t[], int w, double u[], double l[]) {
		for (int i = 0; i < t.length; i++) {
			double max = -Double.MAX_VALUE;
			double min = Double.MAX_VALUE;
			
			for (int j = Math.max(0,  i-w); j <= Math.min(t.length-1, i+w); j++) {
				if (t[j]>max) max = t[j];
				if (t[j]<min)min = t[j];
			}
			
			u[i] = max;
			l[i] = min;
		}
	}
	
	public static void lemireGetEnvelopes(double t[], int w, double u[], double l[]) {
		if (du == null) {
			du = new ArrayDeque<Integer>(2 * w + 2);
		}
		else du.clear();
		
		if (dl == null) {
			dl = new ArrayDeque<Integer>(2 * w + 2);
		}
		else dl.clear();

		du.addLast(0);
		dl.addLast(0);

		if (w >= t.length)
			w = t.length - 1;

		for (int i = 1; i <= w; i++) {
			if (t[i] > t[i - 1]) {
				du.removeLast();
				while (du.size() > 0 && t[i] > t[du.peekLast()])
					du.removeLast();
			} else {
				dl.removeLast();
				while (dl.size() > 0 && t[i] <= t[dl.peekLast()])
					dl.removeLast();
			}
			du.addLast(i);
			dl.addLast(i);
		}
		
		int duFirst = du.peekFirst();
		int dlFirst = dl.peekFirst();

		for (int i = w+1; i < t.length; i++) {
			u[i - w - 1] = t[duFirst];
			l[i - w - 1] = t[dlFirst];

			if (t[i] > t[i - 1]) {
				du.removeLast();
				while (du.size() > 0 && t[i] > t[du.peekLast()])
					du.removeLast();
				if (du.isEmpty()) duFirst = i;
			} else {
				dl.removeLast();
				while (dl.size() > 0 && t[i] <= t[dl.peekLast()])
					dl.removeLast();
				if(dl.isEmpty()) dlFirst = i;
			}
			du.addLast(i);
			dl.addLast(i);
			if (i == 2 * w + 1 + duFirst){
				du.removeFirst();
				duFirst = du.peekFirst();
			}
			else if (i == 2 * w + 1 + dlFirst) {
				dl.removeFirst();
				dlFirst = dl.peekFirst();
			}
		}
		
		for (int i = t.length - w - 1; i < t.length-1; i++) {
			u[i] = t[duFirst];
			l[i] = t[dlFirst];
			if (i - duFirst >= w) {
				du.removeFirst();
				duFirst = du.peekFirst();
			}
			if (i - dlFirst >= w) {
				dl.removeFirst();
				dlFirst = dl.peekFirst();
			}
		}
		
		int index = t.length - 1;
		u[index] = t[duFirst];
		l[index] = t[dlFirst];
	}
	

	public static void lemireGetLower(double t[], int w, double l[]) {
		if (dl == null) {
			dl = new ArrayDeque<Integer>(2 * w + 2);
		}
		else dl.clear();

		dl.addLast(0);

		if (w >= t.length)
			w = t.length - 1;

		for (int i = 1; i <= w; i++) {
			if (t[i] < t[i - 1]) {
	            dl.removeLast();
	            while (dl.size()>0 && t[i] <= t[dl.peekLast()])
	            	dl.removeLast();
			}
			dl.addLast(i);
		}
		
		int dlFirst = dl.peekFirst();

		for (int i = w+1; i < t.length; i++) {
			l[i - w - 1] = t[dlFirst];

			if (t[i] < t[i - 1]) {
				dl.removeLast();
				while (dl.size() > 0 && t[i] <= t[dl.peekLast()])
					dl.removeLast();
				if(dl.isEmpty()) dlFirst = i;
			}
			dl.addLast(i);
			if (i == 2 * w + 1 + dlFirst) {
				dl.removeFirst();
				dlFirst = dl.peekFirst();
			}
		}
		
		for (int i = t.length - w - 1; i < t.length-1; i++) {
			l[i] = t[dlFirst];
			if (i - dlFirst >= w) {
				dl.removeFirst();
				dlFirst = dl.peekFirst();
			}
		}
		
		l[t.length - 1] = t[dlFirst];
	}


	public static void lemireGetUpper(double t[], int w, double u[]) {
		if (du == null) {
			du = new ArrayDeque<Integer>(2 * w + 2);
		}
		else du.clear();

		du.addLast(0);

		if (w >= t.length)
			w = t.length - 1;

		for (int i = 1; i <= w; i++) {
			if (t[i] > t[i - 1]) {
				du.removeLast();
				while (du.size() > 0 && t[i] > t[du.peekLast()])
					du.removeLast();
			}
			du.addLast(i);
		}
		
		int duFirst = du.peekFirst();

		for (int i = w+1; i < t.length; i++) {
			u[i - w - 1] = t[duFirst];

			if (t[i] > t[i - 1]) {
				du.removeLast();
				while (du.size() > 0 && t[i] > t[du.peekLast()])
					du.removeLast();
				if (du.isEmpty()) duFirst = i;
			}
			du.addLast(i);
			if (i == 2 * w + 1 + duFirst){
				du.removeFirst();
				duFirst = du.peekFirst();
			}
		}
		
		for (int i = t.length - w - 1; i < t.length-1; i++) {
			u[i] = t[duFirst];
			if (i - duFirst >= w) {
				du.removeFirst();
				duFirst = du.peekFirst();
			}
		}
		
		u[t.length - 1] = t[duFirst];
	}

	public static double dist(double v1, double v2) {
		double d = v1-v2;
		
		return d*d;
	}
	
    static Deque<Integer> du = null;
    static Deque<Integer> dl = null;
	static double proj[] = null;
	static double up[] = null;
	static double lp[] = null;
	static double sq[] = null;
	static double st[] = null;
	static boolean isFree[] = null;
	static int defaultAllocSize = 3000;	// amount of memory to allocate to an array that should accommodate most sequences
}
