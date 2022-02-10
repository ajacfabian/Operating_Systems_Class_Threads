import java.util.*;
import java.util.concurrent.Semaphore;

public class AFabian_Coaster_Thread
{
	public static void main(String[] args)
	{
		try
		{
			AFabian_Coaster_Thread Go = new AFabian_Coaster_Thread();
			Go.start();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	void start()
	{
		int queueLength = 10;
		rideQueue Buff = new rideQueue(queueLength);

		Semaphore mutex = new Semaphore(1);
		Semaphore full = new Semaphore(0);
		Semaphore empty = new Semaphore(queueLength);

		Rider rider1 = new Rider(Buff, mutex, empty, full, 1, queueLength);

		coasterCar car = new coasterCar(Buff, mutex, empty, full);

		try
		{
			rider1.start();
			Thread.sleep(250);

			car.start();

			Thread.sleep(35000);

			System.out.println("Park's closed, no more rides today");

			rider1.stop = true;

			car.stop();

		}

		catch (InterruptedException e)
		{}
	}

	class rideQueue
	{
		private int[] buf;
		private int in = 0;
		private int out = 0;
		int count = 0;
		private int size;

		rideQueue(int size)
		{
			this.size = size;
			buf = new int[size];
		}

		public void put(int o)
		{
			buf[in] = o;
			++count;
			in = (in + 1) % size;
		}

		public int get()
		{
			int retVal = buf[out];
			--count;
			out = (out + 1) % size;
			return retVal;
		}

		public int count()
		{
			return count;
		}
	}

	class Rider extends Thread
	{
		public boolean stop = false;
		String name;

		rideQueue buf;
		Semaphore mutex, empty, full;
		int value, size;
		int rego = 0;

		Rider(rideQueue b, Semaphore m, Semaphore e, Semaphore f, int val, int s)
		{
			buf = b;
			mutex = m;
			empty = e;
			full = f;
			value = val;
			size = s;
		}

		public void run()
		{
			try
			{
				while(!stop)
				{
					if(buf.count() < 6)
					{
						for(int i = 0; i < size; i++)
						{
							empty.acquire();
							mutex.acquire();

							Thread.sleep((long)(Math.random() * 200));

							System.out.println("Rider " + value + " entered the queue");
							buf.put(value);
							value++;
							rego++;

							if(rego%3 == 2 && rego > 3)
							{
								Thread.sleep(500);
								buf.put(value-5);
								System.out.println("  Rider " + (value - 5) + " came back to ride again!");
							}

							mutex.release();
							full.release();
							Thread.sleep(500);
						}
						Thread.sleep(500);
					}
				}
			}

			catch (InterruptedException e)
			{
				System.out.println("Rider: I wasn't finished!");
			}
		}
	}

	class coasterCar extends Thread
	{
		rideQueue buf;
		String name;
		public boolean stop = false;
		Semaphore mutex, empty, full;

		coasterCar(rideQueue b, Semaphore m, Semaphore e, Semaphore f)
		{
			buf = b;
			mutex = m;
			empty = e;
			full= f;
		}

		public void run()
		{
			try
			{
				int value;
				while(!stop)
				{
					if(buf.count() >= 2)
					{
						full.acquire();
						mutex.acquire();

						System.out.println("    These riders rode the roller coaster:");

						for(int i = 0; i <= buf.count(); i++)
						{
							value = buf.get();
							System.out.println("        rider " + value);
						}
						Thread.sleep(2000);

						System.out.println("    And then they got off the ride");
						Thread.sleep(100);

						mutex.release();
						empty.release();
						Thread.sleep(1000);
					}
					Thread.sleep(1000);
				}
			}

			catch (InterruptedException e)
			{
				System.out.println("Coaster Car: I wasn't finished!");
			}
		}
	}
}