/*
 * Name: Hussachai Puripunpinyo
 * Course No.:  CS 5323
 * Assignment title: PHASE I (March 5)
 * TA's Name: 
 *  - Alireza Boloorchi
 *  - Sukanya Suwisuthikasem
 * Global variables:
 *  - cpu (the reference to CPU)
 *  - memory (the reference to Memory)
 *  - loader (the reference to Loader)
 *  - io (the reference to InputOutput)
 *  - errorHandler (the reference to ErrorHandler)
 *  
 *  Brief Description:
 *  The System is the main entry of program and the container for every subroutines. 
 *  It wires everything together via itself because it exposes the components
 *  via the getter method and pass itself to some components that require.
 *  
 *  Remark:
 *  I should not name this class as System because it will collide with 
 *  the java.lang.System class that is imported by default.
 *  
 */
package hussachai.osu.os2.system;

import hussachai.osu.os2.system.Loader.LoaderContext;
import hussachai.osu.os2.system.cpu.CPU;
import hussachai.osu.os2.system.error.ErrorHandler;
import hussachai.osu.os2.system.error.Errors;
import hussachai.osu.os2.system.error.SystemException;
import hussachai.osu.os2.system.io.InputOutput;
import hussachai.osu.os2.system.storage.Memory;
import hussachai.osu.os2.system.unit.Bit;
import hussachai.osu.os2.system.unit.Word;
import hussachai.osu.os2.system.util.TraceFormatter;

import java.io.File;
import java.math.BigInteger;

/**
 * The system is the main entry of the simulation
 * @author hussachai
 *
 */
public class TheSystem {
	
	private CPU cpu;
	
	private Memory memory;
	
	private Loader loader;
	
	private InputOutput io;
	
	private ErrorHandler errorHandler;
	
	public TheSystem(){
		io = new InputOutput();
		memory = new Memory(this);
		cpu = new CPU(this);
		loader = new Loader(this);
		errorHandler = new ErrorHandler(this);
	}
	
	public void start(String[] fileNames){
		
		File files[] = new File[fileNames.length];
		
		for(int i=0;i<files.length;i++){
			File file = new File(fileNames[i]);
			if(!file.exists()){
				throw new SystemException(Errors.IO_FILE_NOT_FOUND);
			}
			files[i] = file;
		}
		
		for(File file: files){
			
			io.getLog().clearInfo();
			
			try{
				
				io.getLog().info("Cumulative Job ID: 1 (decimal)");
				
				LoaderContext context = loader.load(file);
				/* assign the last instruction word as start address word*/ 
				Word pc = context.instructionWord;
				
				if(context.traceSwitch==Bit.I){
					io.getLog().clearTrace();
					/* write trace header */
					io.getLog().trace("   Trace data in hex format");
					io.getLog().trace(TraceFormatter.getTraceHeader());
				}
				
				cpu.run(pc, context.traceSwitch);
				
			}finally{
				
				String clockHex = new BigInteger(String.valueOf(
						cpu.getClock()), 10).toString(16);
				int inputTime = cpu.getInputTime();
				int outputTime = cpu.getOutputTime();
				io.getLog().info("Clock value: "+clockHex+" (hex)");
				io.getLog().info("Input time: "+inputTime+" (decimal)");
				io.getLog().info("Output time: "+outputTime+" (decimal)");
			}
		}
	}
	
	public CPU getCPU() { return cpu; }
	
	public Memory getMemory() { return memory; }

	public InputOutput getIO() { return io; }

	public ErrorHandler getErrorHandler() { return errorHandler; }
	
	/** entry point **/
	public static void main(String[] args) {
		
		TheSystem system = new TheSystem();
		InputOutput io = system.getIO();
		
		try{
			system.start(args);
			io.getLog().info("Terminated successfully.");
		}catch(SystemException e){
			system.errorHandler.errorHandler(e.getErrorCode());
		}catch(Throwable e){
			system.errorHandler.errorHandler(Errors.SYS_INTERNAL_ERROR);
		}
	}
	
}
