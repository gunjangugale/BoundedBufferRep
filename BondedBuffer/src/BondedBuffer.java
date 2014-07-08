/*
 * Gunjan Gugale	
 * COSC 519
 * Assignment #3 - Bounded Buffer Problem
 *  the producer and the consumer, who share a common, fixed-size buffer used as a queue. 
 *  The producer's job is to generate a piece of data, put it into the buffer and start again. 
 *  At the same time, the consumer is consuming the data (i.e., removing it from the buffer) 
 *  one piece at a time. The problem is to make sure that the producer won't try to add data 
 *  into the buffer if it's full and that the consumer won't try to remove data from an empty buffer.
 */


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Semaphore;

class BondedBuffer
{
	private Semaphore mutex, data, slot;
	private int nextIn, nextOut, size, occupied, ins, outs;
	private String[] Buffer;

	public BondedBuffer(int length)
	{    
		this.size = length;
		Buffer = new String[size];
    
		nextIn = nextOut = occupied = ins = outs = 0;
    
		slot = new Semaphore(size);
		data = new Semaphore(0);
    
		mutex = new Semaphore(1);
	}
	
	public void insertItem(String Item) throws InterruptedException
	{
		slot.acquire();
		mutex.acquire();

		Buffer[nextIn % size] = Item;
		nextIn++;
		occupied++;
		ins++;
 
		mutex.release(); 
		data.release();
	}
 
	public String removeItem() throws InterruptedException
	{
		String Item;

		data.acquire();
		mutex.acquire();

		Item = Buffer[nextOut % size];
		nextOut++;
		occupied--;
		outs++;

		mutex.release();
		slot.release();
 
		return Item;
	}
}

class Producer extends Thread 
{
	public BondedBuffer workspace;
	public String currentItem;

	public Producer(BondedBuffer workspace) 
	{
		this.workspace = workspace;
	}

	public void run()
	{	 
		File file = new File("input.txt");
		StringBuffer contents = new StringBuffer();
		BufferedReader reader = null;
	  
		try 
		{    
			  reader = new BufferedReader(new FileReader(file));           
			  String text = null;
                     
			  while ((text = reader.readLine()) != null) 
			  {                
				  contents.append(text).append(" ");            
			  }   
			  String[] array = (contents.toString()).split(" ");            			     
         
			  for(int i=0; i<array.length; i++)
			  {     				 
				  currentItem = array[i];
				  System.out.println("producer - current item being inserted is " + currentItem);
				  workspace.insertItem(currentItem);
				  sleep((int)(Math.random() * 30));        	
			  }		  
         
		}catch (FileNotFoundException e) 
		{     
			System.out.println("Input File not found");
		} catch (IOException e) 
		{  
			System.out.println("Cannot read Input file");
		}   
		catch (InterruptedException e) 
		{			
			System.out.println("Producer thread exiting now");
		}
 	}
}

class Consumer extends Thread 
{
	public BondedBuffer workspace;
	public String currentItem;

	public Consumer(BondedBuffer workspace) 
	{
		this.workspace = workspace;
	}

	public void run()
	{	 	
		File file =new File("output.txt");
	 
		try 
		{
			while(true)
			{
				currentItem = workspace.removeItem();
				FileWriter fileWritter = new FileWriter(file.getName(),true);
				BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
				bufferWritter.write(" ");
				bufferWritter.write(currentItem);
				bufferWritter.close();			
				System.out.println("consumer - current item being removed is " + currentItem);
				sleep((int)(Math.random() * 30));
			}
		} catch (FileNotFoundException e) 
		{			
			System.out.println ("Output file not found");
		} catch (InterruptedException e) 
		{			
			System.out.println("Threads exiting");   
		} catch (IOException e) 
		{		
			e.printStackTrace();
		}	
	}
}

class BondedBufferProblem 
{
	public static void main(String[] args) throws InterruptedException, IOException 
	{     
		int size = 5;
		if(args.length>0) 
		{
			try 
			{
				size = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) 
			{
				System.err.println("Argument must be an integer.");
			}
		}
     
		File file =new File("output.txt");
		new FileWriter(file.getName());
     
		BondedBuffer space = new BondedBuffer(size);
          
		Producer pthread = new Producer(space);
		Consumer cthread = new Consumer(space);
    
		pthread.start();
        sleep((int)(Math.random() * 30));
        cthread.start();
     
        Thread.sleep(1 * 15 * 1000);
        
        pthread.interrupt();
        cthread.interrupt();           
	}

	private static void sleep(int i) 
	{	
	}
}

