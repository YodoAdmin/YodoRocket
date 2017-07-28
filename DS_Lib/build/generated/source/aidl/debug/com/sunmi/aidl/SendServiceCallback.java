/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\Project\\DualScreenMessager-Lib\\DSC\\DS_Lib\\src\\main\\aidl\\com\\sunmi\\aidl\\SendServiceCallback.aidl
 */
package com.sunmi.aidl;
/** 
 * TODO<请描述这个类是干什么的> 
 * @author 郭晗 
 * @versionCode 1 <每次修改提交前+1>
 */
public interface SendServiceCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.sunmi.aidl.SendServiceCallback
{
private static final java.lang.String DESCRIPTOR = "com.sunmi.aidl.SendServiceCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.sunmi.aidl.SendServiceCallback interface,
 * generating a proxy if needed.
 */
public static com.sunmi.aidl.SendServiceCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.sunmi.aidl.SendServiceCallback))) {
return ((com.sunmi.aidl.SendServiceCallback)iin);
}
return new com.sunmi.aidl.SendServiceCallback.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_sendSuccess:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.sendSuccess(_arg0);
return true;
}
case TRANSACTION_sendError:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _arg1;
_arg1 = data.readInt();
java.lang.String _arg2;
_arg2 = data.readString();
this.sendError(_arg0, _arg1, _arg2);
return true;
}
case TRANSACTION_sendProcess:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
long _arg1;
_arg1 = data.readLong();
long _arg2;
_arg2 = data.readLong();
this.sendProcess(_arg0, _arg1, _arg2);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.sunmi.aidl.SendServiceCallback
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void sendSuccess(int id) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(id);
mRemote.transact(Stub.TRANSACTION_sendSuccess, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void sendError(int id, int errorId, java.lang.String errorInfo) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(id);
_data.writeInt(errorId);
_data.writeString(errorInfo);
mRemote.transact(Stub.TRANSACTION_sendError, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void sendProcess(int id, long totle, long sended) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(id);
_data.writeLong(totle);
_data.writeLong(sended);
mRemote.transact(Stub.TRANSACTION_sendProcess, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
}
static final int TRANSACTION_sendSuccess = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_sendError = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_sendProcess = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
public void sendSuccess(int id) throws android.os.RemoteException;
public void sendError(int id, int errorId, java.lang.String errorInfo) throws android.os.RemoteException;
public void sendProcess(int id, long totle, long sended) throws android.os.RemoteException;
}
