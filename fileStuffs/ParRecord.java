package fileStuffs;

import exceptions.InvalidRecordParameterException;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.Scanner;

public class ParRecord implements Comparable<ParRecord>{

    //This is a record of a parallelogram
    //          ____________________
    //         /                   /
    //        /                   /
    //       /                   /
    //      /___________________/    :3
    public static final int size = 16;
    private int key;
    //side lengths
    private float a;
    private float b;
    //angle in radians
    private float angle;

    public ParRecord(int key, float a, float b, float angle){
        if(key <= 0) throw new InvalidRecordParameterException();
        this.key = key;
        setA(a);
        setB(b);
        setAngle(angle);
    }

    //setters
    public void setA(float a) {
        if (a > 0) this.a = a;
        else
            throw new InvalidRecordParameterException();
    }

    public void setB(float b) {
        if (b > 0) this.b = b;
        else
            throw new InvalidRecordParameterException();
    }

    public void setAngle(float angle) {
        if (angle > 0 && angle < (Math.PI/2)) this.angle = angle;
        else
            throw new InvalidRecordParameterException();
    }

    //getters
    public float getA(){
        return this.a;
    }
    public float getB(){
        return this.b;
    }
    public float getAngle(){
        return this.angle;
    }
    //last two methods are needed for the assignment to be graded well
    public float getField(){
        return (float)(a*Math.sin(angle)*b);
    }
    public int getKey(){return key;}

    static final float maxRandomSideValue = 1000_000f;
    private static final long seed = 42069553;
    private static final Random random = new Random(seed);
    public static ParRecord[] getRandomRecords(int numOfRandRecords){
        ParRecord[] output = new ParRecord[numOfRandRecords];

        int key;
        float a;
        float b;
        float angle;

        for(int i=0;i<numOfRandRecords;i++){
            key = Math.abs(random.nextInt() - 1)+1;
            a = random.nextFloat()*maxRandomSideValue;
            b = random.nextFloat()*maxRandomSideValue;
            //min + rand*(max-min)
            angle = 0 + random.nextFloat()*((float)(Math.PI/2));

            output[i] = new ParRecord(key, a,b,angle);
        }

        return output;
    }
    @Override
    public int compareTo(ParRecord other) {
        return Integer.compare(this.getKey(),other.getKey());
    }

    @Override
    public String toString() {
        return String.valueOf(getKey());
    }

    public ParRecord copy(){
        return new ParRecord(getKey(), getA(),getB(),getAngle());
    }

    private static final int genKeyStart = 10000;
    //genKeysNum starts at -1, so that the first generated key will set it to 0;
    private static int genKeysNum = -1;
    private static int generateSeqKey(){
        genKeysNum +=1;
        return genKeyStart+genKeysNum;
    }

    public static ParRecord getRecordFromConsole(){
        int key;
        float a,b,angle;
        Scanner recScanner = new Scanner(System.in);
        System.out.println("Reading a record from console.\nPlease adhere to this format: \"int float float float\"(key a b angle)");

        key = recScanner.nextInt();
        a = recScanner.nextFloat();
        b = recScanner.nextFloat();
        angle = recScanner.nextFloat();
        recScanner.nextLine();

        return new ParRecord(key,a,b,angle);
    }

    public static ParRecord getRecordFromBytes(byte[] bytes){
        ByteBuffer bBuffer = ByteBuffer.allocate(size);
        bBuffer.put(bytes);
        bBuffer.flip();
        return new ParRecord(
                bBuffer.getInt(),
                bBuffer.getFloat(),
                bBuffer.getFloat(),
                bBuffer.getFloat()
        );
    }

    public byte[] toBytes(){
        ByteBuffer bBuffer = ByteBuffer.allocate(size);
        bBuffer.putInt(key);
        bBuffer.putFloat(this.getA());
        bBuffer.putFloat(this.getB());
        bBuffer.putFloat(this.getAngle());
        return bBuffer.array();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ParRecord)) return false;
        if (obj == this) return true;
        if (
                ((ParRecord)obj).getKey() != this.key ||
                        ((ParRecord)obj).getA() != this.a ||
                        ((ParRecord)obj).getB() != this.b ||
                        ((ParRecord)obj).getAngle() != this.angle
        ) return false;
        return true;
    }

}
