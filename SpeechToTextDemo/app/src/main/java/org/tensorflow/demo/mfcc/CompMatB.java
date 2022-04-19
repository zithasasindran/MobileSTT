package org.tensorflow.demo.mfcc;
import org.apache.commons.math3.linear.*;

public class CompMatB {

    private static double[][] diag(int array){
        double[][] diagarr=new double[array][array];
        for(int i=0;i<array;i++){
            diagarr[i][i] = 1;
        }
        return diagarr;
    }

    private static RealMatrix cumprod(RealMatrix array) {
        double[][] outarr= new double[array.getRowDimension()][array.getColumnDimension()];
        RealMatrix outarr_mat = new Array2DRowRealMatrix(outarr);
        for(int j=0;j<array.getRowDimension();j++) {
                outarr_mat.addToEntry(j,0,array.getEntry(j,0));
        }
        for(int j=0;j<array.getRowDimension();j++) {
            for (int i = 1; i < array.getColumnDimension(); i++) {
                outarr_mat.addToEntry(j,i,array.getEntry(j,i)*array.getEntry(j,(i-1)));
            }
        }
        return outarr_mat;
    }

    private static RealMatrix concat(RealMatrix mat1,RealMatrix mat2) {
        int col = (mat1.getColumnDimension()+mat2.getColumnDimension());
        int row = mat1.getRowDimension();
        double[][] outarr = new double[row][col];
        for(int j=0;j<row;j++) {
            for (int i = 0; i < mat1.getColumnDimension(); i++) {
                outarr[j][i]=mat1.getEntry(j,i);
                }
            for (int k = 0; k < mat2.getColumnDimension(); k++) {
                outarr[j][(mat1.getColumnDimension()+k)]=mat2.getEntry(j,k);
            }
        }
        RealMatrix outarr_mat = new Array2DRowRealMatrix(outarr);
        return outarr_mat;
    }
    private static double[][] ones(int row, int col){
        double[][] outarr = new double[row][col];
        for(int j=0;j<row;j++) {
            for (int i = 0; i < col; i++) {
                outarr[j][i] = 1.0;
            }
        }
        return outarr;
    }
    private static double[][] zeros(int row, int col){
        double[][] outarr = new double[row][col];
        for(int j=0;j<row;j++) {
            for (int i = 0; i < col; i++) {
                outarr[j][i] = 0.0;
            }
        }
        return outarr;
    }
    private static RealMatrix prod(int val){
        double[] out= new double[1];
        out[0]=1;
        for(int j=val;j>0;j--) {
            out[0] = out[0]*j;
        }
        RealMatrix out_mat = new Array2DRowRealMatrix(out);
        return out_mat;
    }

    public static double[][] transpose(double[][] array){
        int i=array.length; //number of rows
        int j=array[0].length; //number of columns
        double[][] b=new double[j][i];
        double[][] a;
        a=array;
        for(int r = 0;r<j;r++) {
            for (int c = 0; c < i; c++) {
                b[r][c] = a[c][r];
            }
        }
        return b;
    }

    public RealMatrix process(double[] x,int n,int dn,double[] x0,double[] Weight){
        //create a 3d matrix of weight
        double[][] W = new double[Weight.length][Weight.length];
        for(int i=0;i<Weight.length;i++){
            W[i][i] = Weight[i];
        }

        int Nx  = x.length;
        int Nx0 = x0.length;

        //compute df
        //double[][] cumprodin = new double [x.length][2];
        RealMatrix Nx_ones_mat = new Array2DRowRealMatrix(ones(Nx,1));
        RealMatrix x_mat = new Array2DRowRealMatrix(x);
        RealMatrix ones_n_1_mat = new Array2DRowRealMatrix(ones(1,n));
        RealMatrix x_mul_ones_n_1_mat = x_mat.multiply(ones_n_1_mat);
        RealMatrix comprodin = concat(Nx_ones_mat,x_mul_ones_n_1_mat);
        RealMatrix cumprodout_mat = cumprod(comprodin);
        double[][] eyeNx = diag(Nx);
        RealMatrix eyeNx_mat = new Array2DRowRealMatrix(eyeNx);

        QRDecomposition qrdecompose = new QRDecomposition(cumprodout_mat);
        DecompositionSolver solver_QR = qrdecompose.getSolver();
        RealMatrix df = solver_QR.solve(eyeNx_mat);
        RealMatrix df_transpose = df.transpose();

        //hx = [(zeros(Nx0,dn)) ones(Nx0,1)*prod(1:dn)];
        RealMatrix zeroes_Nx0_dn = new Array2DRowRealMatrix(zeros(Nx0,dn));
        RealMatrix ones_Nx0_1_mat = new Array2DRowRealMatrix(ones(Nx0,1));
        RealMatrix prod_dn=prod(dn);
        RealMatrix ones_Nx0_1_prod_mat = ones_Nx0_1_mat.multiply(prod_dn);
        RealMatrix hx = concat(zeroes_Nx0_dn,ones_Nx0_1_prod_mat);
        RealMatrix hx_transpose = hx.transpose();

        //filter coeffs
        RealMatrix df_x_hw = df_transpose.multiply(hx_transpose);
        RealMatrix W_mat = new Array2DRowRealMatrix(W);
        RealMatrix fc = df_x_hw.multiply(W_mat);

        return fc;
    }
    /* Function:
            %       Savitzky-Golay Smoothing and Differentiation Filter
%       The Savitzky-Golay smoothing/differentiation filter (i.e., the
%       polynomial smoothing/differentiation filter, or  the least-squares
                                                                     %       smoothing/differentiation filters) optimally fit a set of data
%       points to polynomials of different degrees.
            %       See for details in Matlab Documents (help sgolay). The sgolay
%       function in Matlab can deal with only symmetrical and uniformly
%       spaced data of even number.
%       This function presented here is a general implement of the sgolay
%       function in Matlab. The Savitzky-Golay filter coefficients for even
%       number, nonsymmetrical and nonuniformly spaced data can be
%       obtained. And the filter coefficients for the initial point or the
%       end point can be obtained too. In addition, either numerical
%       results or symbolical results can be obtained. Lastly, this
            %       function is faster than MATLAB's sgolay.
            %
            % Usage:
            %       [fc,df] = savitzkyGolay(x,n,dn,x0,flag)
%   input:
            %       x    = the original data point, e.g., -5:5
            %       n    = polynomial order
%       dn   = differentation order (0=smoothing),  default=0
            %       x0   = estimation point, can be a vector    default=0
            %       W    = weight vector, can be empty
%              must have same length as x0          default=identity
%       flag = numerical(0) or symbolical(1),       default=0
            %
            %   output:
            %       fc   = filter coefficients obtained (B output of sgolay).
            %       df   = differentiation filters (G output of sgolay).
            % Notes:
            % 1.    x can be arbitrary, e.g., odd number or even number, symmetrical or
%       nonsymmetrical, uniformly spaced or nonuniformly spaced, etc.
% 2.    x0 can be arbitrary, e.g., the initial point, the end point, etc.
% 3.    Either numerical results or symbolical results can be obtained.
% Example:
            %       sgsdf([-3:3],2,0,0,[],0)
%       sgsdf([-3:3],2,0,0,[],1)
%       sgsdf([-3:3],2,0,-3,[],1)
%       sgsdf([-3:3],2,1,2,[],1)
%       sgsdf([-2:3],2,1,1/2,[],1)
%       sgsdf([-5:2:5],2,1,0,[],1)
%       sgsdf([-1:1 2:2:8],2,0,0,[],1)
% Author:
            %       Diederick C. Niehorster <dcniehorster@hku.hk> 2011-02-05
            %       Department of Psychology, The University of Hong Kong
%
        %       Originally based on
%       http://www.mathworks.in/matlabcentral/fileexchange/4038-savitzky-golay-smoothing-and-differentiation-filter
            %       Allthough I have replaced almost all the code (partially based on
            %       the comments on the FEX submission), increasing its compatibility
%       with MATLABs sgolay (now supports a weight matrix), its numerical
%       stability and it speed. Now, the help is pretty much all that
%       remains.
%       Jianwen Luo <luojw@bme.tsinghua.edu.cn, luojw@ieee.org> 2003-10-05
            %       Department of Biomedical Engineering, Department of Electrical Engineering
%       Tsinghua University, Beijing 100084, P. R. China
% Reference
%[1]A. Savitzky and M. J. E. Golay, "Smoothing and Differentiation of Data
            %   by Simplified Least Squares Procedures," Analytical Chemistry, vol. 36,
            %   pp. 1627-1639, 1964.
            %[2]J. Steinier, Y. Termonia, and J. Deltour, "Comments on Smoothing and
            %   Differentiation of Data by Simplified Least Square Procedures,"
            %   Analytical Chemistry, vol. 44, pp. 1906-1909, 1972.
            %[3]H. H. Madden, "Comments on Savitzky-Golay Convolution Method for
            %   Least-Squares Fit Smoothing and Differentiation of Digital Data,"
            %   Analytical Chemistry, vol. 50, pp. 1383-1386, 1978.
            %[4]R. A. Leach, C. A. Carter, and J. M. Harris, "Least-Squares Polynomial
            %   Filters for Initial Point and Slope Estimation," Analytical Chemistry,
            %   vol. 56, pp. 2304-2307, 1984.
            %[5]P. A. Baedecker, "Comments on Least-Square Polynomial Filters for
            %   Initial Point and Slope Estimation," Analytical Chemistry, vol. 57, pp.
            %   1477-1479, 1985.
            %[6]P. A. Gorry, "General Least-Squares Smoothing and Differentiation by
            %   the Convolution (Savitzky-Golay) Method," Analytical Chemistry, vol.
            %   62, pp. 570-573, 1990.
            %[7]Luo J W, Ying K, He P, Bai J. Properties of Savitzky-Golay Digital
%   Differentiators, Digital Signal Processing, 2005, 15(2): 122-136.
            %
            %See also:
            %       sgolay, savitzkyGolayFilt
% Check if the input arguments are valid and apply defaults*/

}
