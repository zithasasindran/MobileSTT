package org.tensorflow.demo.Denoising;
// MATLAB Coder version            : 4.0
// C/C++ source code generated on  : 28-Oct-2020 00:42:22

/*
Description: MATLAB expint() Implimentation in JAVA
Test 1: expint(2) => 0.04890051070806113
Test 2: expint(-1) => -1.8951178163559366 -3.141592653589793i
Developer :Sanath Sunkad
Version : Eint 1.0
*/

 import java.lang.Math;
 import java.util.*;
 
public class Eint{

//  // Main program to example to call Eint() function   **Can be changed or removed**
//	public static double main(double number){
//		//Scanner reader = new Scanner(System.in);
//        System.out.print("Enter a number for eint comp: " + number);
//		//number = reader.nextDouble();
//		Creal_T ans =new Creal_T();
//		ans=Eint(number);
//		double final_ans;
//		if(number > 0){
//		    final_ans=ans.re;
//			System.out.print(ans.re);
//		}
//		else{
//            final_ans=ans.re;
//			System.out.print(ans.re+" "+ans.im+"i");
//		}
//		return final_ans;
//	}
//// Main End
	
//declaration and definition of math functions
    private static double exp(final double x)
    {
        return Math.exp(x);
    }

    private static double abs(final double x)
    {
        return Math.abs(x);
    }

    private static double log(final double x)
    {
        return Math.log(x);
    }
	private static double sqrt(final double x)
    {
        return Math.sqrt(x);
    }
	private static double scalb(final double x, final int y)
    {
        return Math.scalb(x,y); // CPP ldexp() equivalent is scalb() in JAVA
    }
	
//class declaration to contain exponent and mantissa for frexp() function **Can be used by creating new class**
	public static class FRexpResult
	{
	   public int exponent = 0;
	   public double mantissa = 0.;
	}
	
	// frexp() CPP code implimentaion in JAVA 
	public static FRexpResult frexp(double value)
	{
	   final FRexpResult result = new FRexpResult();
	   long bits = Double.doubleToLongBits(value);
	   double realMant = 1.;

	   // Test for NaN, infinity, and zero.
	   if (Double.isNaN(value) || 
		   value + value == value || 
		   Double.isInfinite(value))
	   {
		  result.exponent = 0;
		  result.mantissa = value;
	   }
	   else
	   {

		  boolean neg = (bits < 0);
		  int exponent = (int)((bits >> 52) & 0x7ffL);
		  long mantissa = bits & 0xfffffffffffffL;

		  if(exponent == 0)
		  {
			 exponent++;
		  }
		  else
		  {
			 mantissa = mantissa | (1L<<52);
		  }

		  // bias the exponent - actually biased by 1023.
		  // we are treating the mantissa as m.0 instead of 0.m
		  //  so subtract another 52.
		  exponent -= 1075;
		  realMant = mantissa;

		  // normalize
		  while(realMant > 1.0) 
		  {
			 mantissa >>= 1;
			 realMant /= 2.;
			 exponent++;
		  }

		  if(neg)
		  {
			 realMant = realMant * -1;
		  }

		  result.exponent = exponent;
		  result.mantissa = realMant;
	   }
	   return result;
	}
	
// class declaration for Creal_T to contain real and imaginary part of exp_int object **Can be used by creating new class**
	public static class Creal_T{
		public double re = 0;
		public double im = 0;
	}
	
	
// Function Declarations
//static double rt_hypotd(double u0, double u1);

// Function Definitions

//
// Arguments    : double u0
//                double u1
// Return Type  : double
//

static double rt_hypotd(double u0, double u1)
{
  double y;
  double a;
  double b;
  a = abs(u0);
  b = abs(u1);
  if (a < b) {
    a /= b;
    y = b * sqrt(a * a + 1.0);
  } else if (a > b) {
    b /= a;
    y = a * sqrt(b * b + 1.0);
  } else {
    y = a * 1.4142135623730951;
  }

  return y;
}

//
// Arguments    : double val
// Return Type  : creal_T
//
// Eint must include data type Creal_T
public static Creal_T Eint(double val)
{
  FRexpResult fre=new FRexpResult();   //object to hold exponent and mantiss of frexp()
  final Creal_T exp_int = new Creal_T(); // exp_int Creal_T object
  double pk;
  int k;
  double dv0[] = new double[]{ -3.6026936263360228E-9, -4.81953845214096E-7,
    -2.5694983221159331E-5, -0.000697379085953419, -0.010195735298457921,
    -0.078118635592481972, -0.30124328927627148, -0.77738073257355289,
    8.2676619523664776 };

  double am2_re;
  double oldf_re;
  double am2_im;
  double oldf_im;
  double bm2_re;
  double bm2_im;
  double am1_re;
  double am1_im;
  double j;
  double bm1_re;
  double f_re;
  double term;
  double f_im;
  int exitg1;
  double absxk;
  int exponent;
  double b_re;
  double b_im;
  double d;

  pk = -3.6026936263360228E-9;
  for (k = 0; k < 8; k++) {
    pk = val * pk + dv0[k + 1];
  }

  if (val == 0.0) {
    exp_int.re = 1.7976931348623157E+308;
    exp_int.im = 0.0;
  } else if (0.0 <= pk) {
    if (val < 0.0) {
      oldf_re = log(abs(val));
      oldf_im = 3.1415926535897931;
    } else {
      oldf_re = log(val);
      oldf_im = 0.0;
    }

    exp_int.re = -0.57721566490153287 - oldf_re;
    exp_int.im = 0.0 - oldf_im;
    j = 1.0;
    pk = val;
    term = val;
    do {
      exitg1 = 0;
      absxk = rt_hypotd(exp_int.re, exp_int.im);
      if (absxk <= 2.2250738585072014E-308) {
        absxk = 4.94065645841247E-324;
      } else {
        fre=frexp(absxk); 					   // use of frexp()
        absxk = scalb(1.0, fre.exponent - 53); // use of scalb
      }

      if (abs(term) > absxk) {
        exp_int.re+= term;
        j++;
        pk = -val * pk / j;
        term = pk / j;
      } else {
        exitg1 = 1;
      }
    } while (exitg1 == 0);
  } else if (0.0 > pk) {
    am2_re = 0.0;
    am2_im = 0.0;
    bm2_re = 1.0;
    bm2_im = 0.0;
    am1_re = 1.0;
    am1_im = 0.0;
    bm1_re = val;
    f_re = 1.0 / val;
    f_im = 0.0;
    oldf_re = 1.7976931348623157E+308;
    oldf_im = 0.0;
    j = 2.0;
    do {
      exitg1 = 0;
      oldf_re = f_re - oldf_re;
      oldf_im = f_im - oldf_im;
      absxk = rt_hypotd(f_re, f_im);
      if (absxk <= 2.2250738585072014E-308) {
        absxk = 4.94065645841247E-324;
      } else {
        //exp(absxk, exponent);
        //absxk = exp(1.0, exponent - 53);
		fre=frexp(absxk);
		absxk = scalb(1.0, fre.exponent - 53); // use of scalb
      }

      if (rt_hypotd(oldf_re, oldf_im) > 100.0 * absxk) {
        absxk = j / 2.0;
        oldf_re = absxk * am2_re;
        oldf_im = absxk * am2_im;
        b_re = bm1_re + absxk * bm2_re;
        b_im = absxk * bm2_im;
        if (b_im == 0.0) {
          if (am1_im == 0.0) {
            am2_re = am1_re / b_re;
            am2_im = 0.0;
          } else if (am1_re == 0.0) {
            am2_re = 0.0;
            am2_im = am1_im / b_re;
          } else {
            am2_re = am1_re / b_re;
            am2_im = am1_im / b_re;
          }
        } else if (b_re == 0.0) {
          if (am1_re == 0.0) {
            am2_re = am1_im / b_im;
            am2_im = 0.0;
          } else if (am1_im == 0.0) {
            am2_re = 0.0;
            am2_im = -(am1_re / b_im);
          } else {
            am2_re = am1_im / b_im;
            am2_im = -(am1_re / b_im);
          }
        } else {
          term = b_re / b_im;
          d = b_im + term * b_re;
          am2_re = (term * am1_re + am1_im) / d;
          am2_im = (term * am1_im - am1_re) / d;
        }

        pk = am1_re + oldf_re;
        am1_im += oldf_im;
        if (b_im == 0.0) {
          if (am1_im == 0.0) {
            am1_re = pk / b_re;
            am1_im = 0.0;
          } else if (pk == 0.0) {
            am1_re = 0.0;
            am1_im /= b_re;
          } else {
            am1_re = pk / b_re;
            am1_im /= b_re;
          }
        } else if (b_re == 0.0) {
          if (pk == 0.0) {
            am1_re = am1_im / b_im;
            am1_im = 0.0;
          } else if (am1_im == 0.0) {
            am1_re = 0.0;
            am1_im = -(pk / b_im);
          } else {
            am1_re = am1_im / b_im;
            am1_im = -(pk / b_im);
          }
        } else {
          term = b_re / b_im;
          d = b_im + term * b_re;
          am1_re = (term * pk + am1_im) / d;
          am1_im = (term * am1_im - pk) / d;
        }

        f_re = am1_re;
        f_im = am1_im;
        j++;
        absxk = (j - 1.0) / 2.0;
        oldf_re = absxk * am2_re;
        oldf_im = absxk * am2_im;
        if (b_im == 0.0) {
          term = bm1_re / b_re;
          pk = 0.0;
        } else if (b_re == 0.0) {
          if (bm1_re == 0.0) {
            term = 0.0 / b_im;
            pk = 0.0;
          } else {
            term = 0.0;
            pk = -(bm1_re / b_im);
          }
        } else {
          term = b_re / b_im;
          d = b_im + term * b_re;
          term = term * bm1_re / d;
          pk = (0.0 - bm1_re) / d;
        }

        b_re = val + absxk * term;
        b_im = absxk * pk;
        if (b_im == 0.0) {
          if (am1_im == 0.0) {
            am2_re = am1_re / b_re;
            am2_im = 0.0;
          } else if (am1_re == 0.0) {
            am2_re = 0.0;
            am2_im = am1_im / b_re;
          } else {
            am2_re = am1_re / b_re;
            am2_im = am1_im / b_re;
          }

          bm2_re = 1.0 / b_re;
          bm2_im = 0.0;
        } else {
          if (b_re == 0.0) {
            if (am1_re == 0.0) {
              am2_re = am1_im / b_im;
              am2_im = 0.0;
            } else if (am1_im == 0.0) {
              am2_re = 0.0;
              am2_im = -(am1_re / b_im);
            } else {
              am2_re = am1_im / b_im;
              am2_im = -(am1_re / b_im);
            }
          } else {
            term = b_re / b_im;
            d = b_im + term * b_re;
            am2_re = (term * am1_re + am1_im) / d;
            am2_im = (term * am1_im - am1_re) / d;
          }

          if (b_re == 0.0) {
            bm2_re = 0.0;
            bm2_im = -(1.0 / b_im);
          } else {
            term = b_re / b_im;
            d = b_im + term * b_re;
            bm2_re = term / d;
            bm2_im = -1.0 / d;
          }
        }

        pk = val * am1_re + oldf_re;
        am1_im = val * am1_im + oldf_im;
        if (b_im == 0.0) {
          if (am1_im == 0.0) {
            am1_re = pk / b_re;
            am1_im = 0.0;
          } else if (pk == 0.0) {
            am1_re = 0.0;
            am1_im /= b_re;
          } else {
            am1_re = pk / b_re;
            am1_im /= b_re;
          }
        } else if (b_re == 0.0) {
          if (pk == 0.0) {
            am1_re = am1_im / b_im;
            am1_im = 0.0;
          } else if (am1_im == 0.0) {
            am1_re = 0.0;
            am1_im = -(pk / b_im);
          } else {
            am1_re = am1_im / b_im;
            am1_im = -(pk / b_im);
          }
        } else {
          term = b_re / b_im;
          d = b_im + term * b_re;
          am1_re = (term * pk + am1_im) / d;
          am1_im = (term * am1_im - pk) / d;
        }

        bm1_re = 1.0;
        oldf_re = f_re;
        oldf_im = f_im;
        f_re = am1_re;
        f_im = am1_im;
        j++;
      } else {
        exitg1 = 1;
      }
    } while (exitg1 == 0);

    exp_int.re = exp(-val) * f_re;
    exp_int.im = exp(-val) * f_im;
    if (val < 0.0) {
      exp_int.im -= 3.1415926535897931;
    }
  } else {
    exp_int.re = 0.0;
    exp_int.im = 0.0;
  }

  return exp_int;
}

//
// File trailer for Eint.java
//
// [EOF]
//
}