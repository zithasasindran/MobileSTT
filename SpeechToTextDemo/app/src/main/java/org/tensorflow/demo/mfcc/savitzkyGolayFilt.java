package org.tensorflow.demo.mfcc;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

public class savitzkyGolayFilt {
    private static double[][] zeros(int row, int col){
        double[][] outarr = new double[row][col];
        for(int j=0;j<row;j++) {
            for (int i = 0; i < col; i++) {
                outarr[j][i] = 0.0;
            }
        }
        return outarr;
    }

    private static RealMatrix flipud(RealMatrix mat) {
        int col = mat.getColumnDimension();
        int row = mat.getRowDimension();
        double[][] outarr = new double[row][col];
        for(int j=0;j<row;j++) {
            for (int i = 0; i < mat.getColumnDimension(); i++) {
                outarr[j][i]=mat.getEntry((mat.getRowDimension()-j-1),(mat.getColumnDimension()-i-1));
            }
        }
        RealMatrix outarr_mat = new Array2DRowRealMatrix(outarr);
        return outarr_mat;
    }

    private static RealMatrix fliplr(RealMatrix mat) {
        int col = mat.getColumnDimension();
        int row = mat.getRowDimension();
        double[][] outarr = new double[row][col];
        for(int j=0;j<row;j++) {
            for (int i = 0; i < mat.getColumnDimension(); i++) {
                outarr[j][i]=mat.getEntry(j,(mat.getColumnDimension()-i-1));
            }
        }
        RealMatrix outarr_mat = new Array2DRowRealMatrix(outarr);
        return outarr_mat;
    }

    private static RealMatrix concat_rows(RealMatrix mat1,RealMatrix mat2) {
        int col = mat1.getColumnDimension();
        int row = (mat1.getRowDimension()+mat2.getRowDimension());
        double[][] outarr = new double[row][col];
        for(int j=0;j<col;j++) {
            for (int i = 0; i < mat1.getRowDimension(); i++) {
                outarr[i][j]=mat1.getEntry(i,j);
            }
            for (int k = 0; k < mat2.getRowDimension(); k++) {
                outarr[(mat1.getRowDimension()+k)][j]=mat2.getEntry(k,j);
            }
        }
        RealMatrix outarr_mat = new Array2DRowRealMatrix(outarr);
        return outarr_mat;
    }

    public double[] process(double[] x, int N, int DN, int F) {

        double[] W=new double[F];
        for(int i=0;i<F;i++){
            W[i]=1;
        }

        double[] pp=new double[F];
        pp[0]=Math.round(-F/2);
        for(int i=1;i<F;i++){
            pp[i]=pp[i-1]+1;
        }

        CompMatB compmatb = new CompMatB();
        RealMatrix B=compmatb.process(pp,N,DN,pp,W);


        //Compute the transient on (note, this is different than in sgolayfilt,
        // they had an optimization leaving out some transposes that is only valid
        // for DN==0)
        //y(1:(F+1)/2-1,:) = fliplr(B(:,(F-1)/2+2:end)).'*flipud(x(1:F,:));
        RealMatrix x_mat = new Array2DRowRealMatrix(x);
        RealMatrix x_sub_F = x_mat.getSubMatrix(0,(F-1),0,(x_mat.getColumnDimension()-1));
        RealMatrix flipud_x_F = flipud(x_sub_F);
        RealMatrix B_sub_F = B.getSubMatrix(0,(B.getRowDimension()-1),((F-1)/2+1),(B.getColumnDimension()-1));
        RealMatrix fliplr_B_F = fliplr(B_sub_F);
        RealMatrix  fliplr_B_F_transpose =  fliplr_B_F.transpose();
        RealMatrix y_part1 = fliplr_B_F_transpose.multiply(flipud_x_F);

        //Compute the steady state output
        //ytemp = filter(B(:,(F-1)./2+1),1,x);
        //y((F+1)/2:end-(F+1)/2+1,:) = ytemp(F:end,:);
        filter dig_filter = new filter();
        double[] b_for_filter = B.getColumn((F-1)/2);
        double[] a=new double[] {1};
        double[] ytemp = dig_filter.filter(b_for_filter,a,x);
        RealMatrix ytemp_mat = new Array2DRowRealMatrix(ytemp);

        //Compute the transient off
        //y(end-(F+1)/2+2:end,:) = fliplr(B(:,1:(F-1)/2)).'*flipud(x(end-(F-1):end,:));
        RealMatrix x_sub_F_2 = x_mat.getSubMatrix((x_mat.getRowDimension()-F),(x_mat.getRowDimension()-1),0,(x_mat.getColumnDimension()-1));
        RealMatrix flipud_x_F_2 = flipud(x_sub_F_2);
        RealMatrix B_sub_F_2 = B.getSubMatrix(0,(B.getRowDimension()-1),0,((F-1)/2)-1);
        RealMatrix fliplr_B_F_2 = fliplr(B_sub_F_2);
        RealMatrix  fliplr_B_F_transpose_2 =  fliplr_B_F_2.transpose();
        RealMatrix y_part2 = fliplr_B_F_transpose_2.multiply(flipud_x_F_2);

        double[] y = new double[x.length];
        int k=0;
        for (int i=0;i<((F+1)/2-1);i++){
            y[i]=y_part1.getEntry(k,0);
            k++;
        }
        k=F-1;
        for (int i=(((F+1)/2)-1);i<(x.length-(F+1)/2+1);i++){
            y[i]=ytemp_mat.getEntry(k,0);
            k++;
        }
        k=0;
        for (int i=(x.length-(F+1)/2+1);i<(x.length);i++){
            y[i]=y_part2.getEntry(k,0);
            k++;
        }


        return y;
    }
}