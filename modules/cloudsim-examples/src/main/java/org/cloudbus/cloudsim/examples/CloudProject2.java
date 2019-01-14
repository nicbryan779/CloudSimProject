package org.cloudbus.cloudsim.examples;

import java.text.DecimalFormat;
import java.util.*;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

//  Project Link : https://www.sciencedirect.com/science/article/pii/S2314717216300848
//  Project Name : Simulation modeling of cloud computing for smart grid using CloudSim
//  Space-Shared Allocation Policy
//  Group Member
/*  1. Steve Vinsensius Jo - 2001621965
	  2. Devin Christian - 2001616523
	  3. Nicolas Bryan - 2001622236
* */
public class CloudProject2 {
	
	// Cloudlet and vmlist
	private static List<Cloudlet> cloudletList;
	private static List<Vm> vmlist;
	
	private static Datacenter createDatacenter(String name) {
		// creating list of hosts for the simulation
		List<Host> hostList = new ArrayList<>();
		// creating CPU cores / PEs for the hosts
		// PE = Processing Element for the CPU to run the process
		// We need 2 PEs
		List<Pe> peList1 = new ArrayList<>();
		List<Pe> peList2 = new ArrayList<>();
		
		// MIPS = Million Instruction per Second
		int mips = 25000;
		
		int num = 34;
		for(int i=0;i<34;i++)
		{
			peList1.add(new Pe(i, new PeProvisionerSimple(mips)));
			peList2.add(new Pe(i, new PeProvisionerSimple(mips)));
		}
		
		// Creating the hosts for processing the data from datacenter
		int Id = 0;
		int RAM = 45568; // 44.5 GB
		long storage = 10000000; // 10000 GB
		int bandwidth = 100000; // 100Gbps
		
		// creating the first host with Id tag = 0
		hostList.add(
			new Host(
				Id,
				new RamProvisionerSimple(RAM),
				new BwProvisionerSimple(bandwidth),
				storage,
				peList1,
				new VmSchedulerSpaceShared(peList1)
			)
		);
		
		Id++;
		
		// creating the second host with Id tag = 1
		hostList.add(
			new Host(
				Id,
				new RamProvisionerSimple(RAM),
				new BwProvisionerSimple(bandwidth),
				storage,
				peList2,
				new VmSchedulerSpaceShared(peList2)
			)
		);
		
		// creating the characteristic for our datacenter
		String architecture = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen"; // virtual machine memory
		double time_zone = 7.0;
		double cost = 3.0;
		double costPerMemory = 0.05;
		double costPerStorage = 0.1;
		double costPerBandwidth = 0.1;
		
		// creating the list of storage for future development
		// if we need to store something in our data center
		LinkedList<Storage> storageList = new LinkedList<>();
		
		DatacenterCharacteristics dcChar = new DatacenterCharacteristics(
			architecture, os, vmm, hostList, time_zone, cost, costPerMemory, costPerStorage, costPerBandwidth
		);
		
		// creating our first data center
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, dcChar, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return datacenter;
	}
	
	private static DatacenterBroker createBroker () {
		DatacenterBroker broker;
		try {
			broker = new DatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}
	
	private static List<Vm> createVM(int id, int num) {
		
		// Create a container to store Vm
		LinkedList<Vm> list = new LinkedList<>();
		
		long size = 256; //image size (MB)
		int ram = 512; // vm memory (MB)
		int mips = 250; // mips number
		long bandwidth = 1000; // 1 Gbps
		int pesNumber = 1;
		String vmm = "Xen"; // vm machine name
		
		// array of Virtual machines
		// specify how many to create
		Vm[] vm = new Vm[num];
		
		for(int i=0; i< num ;i++){
			vm[i] = new Vm(i, id, mips, pesNumber, ram, bandwidth, size, vmm, new CloudletSchedulerSpaceShared());
			
			list.add(vm[i]);
		}
		
		return list;
		
	}
	
	private static List<Cloudlet> createCloudlet(int id, int num) {
		
		// Creates a container to store Cloudlets
		LinkedList<Cloudlet> list = new LinkedList<>();
		
		//cloudlet parameters
		long length = 40000;
		long fileSize = 300;
		long outputSize = 300;
		int pesNumber = 1;
		UtilizationModel utilizationModel = new UtilizationModelFull();
		
		Cloudlet[] cloudlet = new Cloudlet[num];
		
		for(int i=0;i<num;i++){
			cloudlet[i] = new Cloudlet(i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			// setting the owner of these Cloudlets
			cloudlet[i].setUserId(id);
			list.add(cloudlet[i]);
		}
		
		return list;
	}
	
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;
		
		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
			"Data center ID" + indent + "VM ID" + indent + indent + "Time" + indent + "Start Time" + indent + "Finish Time");
		
		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);
			
			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
				Log.print("SUCCESS");
				
				Log.printLine( indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
					indent + indent + indent + dft.format(cloudlet.getActualCPUTime()) +
					indent + indent + dft.format(cloudlet.getExecStartTime())+ indent + indent + indent + dft.format(cloudlet.getFinishTime()));
			}
		}
	}
	
	
	public static void main(String[] args) {
		Log.printLine("Starting Simulation");
		try {
			int num_user = 1;
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;
			
			CloudSim.init(num_user, calendar, trace_flag);
			
			// creating datacenter
			Datacenter datacenter = createDatacenter("Datacenter");
			
			// creating broker and get the id of the broker
			DatacenterBroker broker = createBroker();
			int brokerId = broker.getId();
			
			// creating VM and bind it to the broker's id (vm to specific broker)
			vmlist = createVM(brokerId,68);
			// creating cloudLet
			cloudletList = createCloudlet(brokerId,68);
			
			// make sure that this vm list and cloudlet list are assigned to this broker
			broker.submitVmList(vmlist);
			broker.submitCloudletList(cloudletList);
			
			// start the simulation
			CloudSim.startSimulation();
			
			// Print result
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			
			// stop the simulation
			CloudSim.stopSimulation();
			
			// Print all the cloudlet list
			printCloudletList(newList);
			
			// Last line to finish all the steps
			Log.printLine("Simulation of the model finished!");
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("error during the simulation");
			
		}
	}
	
	
}

