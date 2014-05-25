package com.woopitapp;

import java.util.Vector;

public class Utils {
	    private static void minus(float[] a, float[] b, float[] result) {
			float[] res = (result == null)?a:result;
			for (int i=0;i<Math.min(a.length,b.length);i++)
			    res[i] = a[i]-b[i];
	    }

	    public static void cross(float[] p1, float[] p2, float[] result) {
	    	result[0] = p1[1]*p2[2]-p2[1]*p1[2];
	    	result[1] = p1[2]*p2[0]-p2[2]*p1[0];
	    	result[2] = p1[0]*p2[1]-p2[0]*p1[1];
	        }
	    public static void scalarMultiply(float[] vector, float scalar) {
	    	for (int i=0;i<vector.length;i++)
	    	    vector[i] *= scalar;
	    }
	    public static float magnitude(float[] vector) {
	    	return (float)Math.sqrt(vector[0]*vector[0]+
	    				vector[1]*vector[1]+
	    				vector[2]*vector[2]);
	    }
	    public static void normalize(float[] vector) {
	    	scalarMultiply(vector, 1/magnitude(vector));
	    }

		public static float[] convertFloats(Float[] floats)
		{
			float[] r = new float[floats.length];
		    for (int i=0; i < floats.length; i++)
		    {
		        r[i] = floats[i];
		    }
		    return r;
		}

		public static int parseInt(String val) {
			
			if (val.length() == 0) {
				return -1;
			}
			return Integer.parseInt(val);
		}
		
		public static int[] parseIntTriple(String face) {
			
			int ix = face.indexOf("/");
			if (ix == -1)
				return new int[] {Integer.parseInt(face)-1};
			else {
				int ix2 = face.indexOf("/", ix+1);
				if (ix2 == -1) {
					return new int[] 
					               {Integer.parseInt(face.substring(0,ix))-1,
							Integer.parseInt(face.substring(ix+1))-1};
				}
				else {
					return new int[] 
					               {parseInt(face.substring(0,ix))-1,
							parseInt(face.substring(ix+1,ix2))-1,
							parseInt(face.substring(ix2+1))-1
					               };
				}
			}
		}
}
