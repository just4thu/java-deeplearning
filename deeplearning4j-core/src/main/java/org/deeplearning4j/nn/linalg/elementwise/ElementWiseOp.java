package org.deeplearning4j.nn.linalg.elementwise;


import org.deeplearning4j.nn.linalg.NDArray;

/**
 * An element wise operation over an ndarray
 *
 * @author Adam Gibson
 */
public interface ElementWiseOp {



    /**
     * The input matrix
     * @return
     */
    public NDArray from();


    /**
     * Apply the transformation at from[i]
     * @param i the index of the element to applyTransformToOrigin
     */
    void applyTransformToOrigin(int i);


    /**
     * Apply the transformation at from[i] using the supplied value
     * @param i the index of the element to applyTransformToOrigin
     * @param valueToApply the value to apply to the given index
     */
    void applyTransformToOrigin(int i, double valueToApply);

    /**
     * Get the element at from
     * at index i
     * @param i the index of the element to retrieve
     * @return the element at index i
     */
    double getFromOrigin(int i);

    /**
     * The transformation for a given value
      * @param value the value to applyTransformToOrigin
     *  @param i the index of the element being acted upon
     * @return the transformed value based on the input
     */

    double apply(double value,int i);



}
