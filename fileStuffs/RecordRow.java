package fileStuffs;

import java.nio.ByteBuffer;

public class RecordRow {
    public static final byte EMPTY_STATE = 0, NORMAL_STATE = 1, GUARD_STATE=2;
    public static final int size = ParRecord.size + 9;//9 = 8(long) + 1(state)
    public static final long nullOffset = -1;
    public ParRecord record;
    public long offset;
    public byte stateByte;

    public RecordRow(ParRecord record){
        this.record = record;
        stateByte = NORMAL_STATE;
        offset = nullOffset;
    }

    public byte[] toBytes(){
        byte[] output = new byte[RecordRow.size];
        System.arraycopy(record.toBytes(),0,output,0,ParRecord.size);
        ByteBuffer bBuffer = ByteBuffer.allocate(8);
        bBuffer.putLong(offset);
        System.arraycopy(bBuffer.array(),0,output,ParRecord.size,8);
        output[RecordRow.size-1] = stateByte;
        return output;
    }

    public static RecordRow RecordRowFromBytes(byte[] bytes){
        ByteBuffer bBuffer = ByteBuffer.allocate(RecordRow.size);
        bBuffer.put(bytes);
        bBuffer.flip();

        byte[] recBytes = new byte[ParRecord.size];
        bBuffer.get(recBytes,0,ParRecord.size);

        RecordRow output = new RecordRow(ParRecord.getRecordFromBytes(recBytes));
        output.offset = bBuffer.getLong();
        output.stateByte = bBuffer.get();

        return output;
    }

    public RecordRow copy(){
        RecordRow output = new RecordRow(this.record);
        output.offset = this.offset;
        return output;
    }

    @Override
    public String toString() {
        if (stateByte == EMPTY_STATE) return "null record row";
        if (stateByte == GUARD_STATE) return "Guard row. Pointing at: "+this.offset;
        return "key: " + record.toString() + " offset: " + offset+" state: "+stateByte;
    }

    public static RecordRow randomRecRow(){
        return new RecordRow(ParRecord.getRandomRecords(1)[0]);
    }

    public void copyOnto(RecordRow dest){
        dest.record = this.record.copy();
        dest.stateByte = this.stateByte;
        dest.offset = this.offset;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RecordRow)) return false;
        if (obj == this) return true;
        if (
                ((RecordRow)obj).stateByte != this.stateByte ||
                ((RecordRow)obj).offset != this.offset ||
                !this.record.equals(((RecordRow)obj).record)
        ) return false;
        return true;
    }


}
