package Utils;

import java.util.HashMap;

public class UCRInfo {
	private static void initUCRInfo() {
		window.put("50words",16);
		window.put("Adiac",5);
		window.put("ArrowHead",0);
		window.put("Beef",0);
		window.put("BeetleFly",35);
		window.put("BirdChicken",30);
		window.put("Car",5);
		window.put("CBF",14);
		window.put("ChlorineConcentration",0);
		window.put("CinC_ECG_torso",16);
		window.put("Coffee",0);
		window.put("Computers",93);
		window.put("Cricket_X",30);
		window.put("Cricket_Y",51);
		window.put("Cricket_Z",15);
		window.put("DiatomSizeReduction",0);
		window.put("DistalPhalanxOutlineAgeGroup",0);
		window.put("DistalPhalanxOutlineCorrect",1);
		window.put("DistalPhalanxTW",0);
		window.put("Earthquakes",112);
		window.put("ECG200",0);
		window.put("ECG5000",1);
		window.put("ECGFiveDays",0);
		window.put("ElectricDevices",13);
		window.put("FaceAll",3);
		window.put("FaceFour",7);
		window.put("FacesUCR",15);
		window.put("FISH",18);
		window.put("FordA",0);
		window.put("FordB",5);
		window.put("Gun_Point",0);
		window.put("Ham",0);
		window.put("HandOutlines",27);
		window.put("Haptics",21);
		window.put("Herring",25);
		window.put("InlineSkate",263);
		window.put("InsectWingbeatSound",5);
		window.put("ItalyPowerDemand",0);
		window.put("LargeKitchenAppliances",676);
		window.put("Lighting2",38);
		window.put("Lighting7",15);
		window.put("MALLAT",0);
		window.put("Meat",0);
		window.put("MedicalImages",19);
		window.put("MiddlePhalanxOutlineAgeGroup",4);
		window.put("MiddlePhalanxOutlineCorrect",0);
		window.put("MiddlePhalanxTW",1);
		window.put("MoteStrain",0);
		window.put("NonInvasiveFatalECG_Thorax1",7);
		window.put("NonInvasiveFatalECG_Thorax2",7);
		window.put("OliveOil",0);
		window.put("OSULeaf",29);
		window.put("PhalangesOutlinesCorrect",0);
		window.put("Phoneme",143);
		window.put("Plane",8);
		window.put("ProximalPhalanxOutlineAgeGroup",0);
		window.put("ProximalPhalanxOutlineCorrect",0);
		window.put("ProximalPhalanxTW",4);
		window.put("RefrigerationDevices",57);
		window.put("ScreenType",122);
		window.put("ShapeletSim",15);
		window.put("ShapesAll",20);
		window.put("SmallKitchenAppliances",108);
		window.put("SonyAIBORobotSurface",0);
		window.put("SonyAIBORobotSurfaceII",0);
		window.put("StarLightCurves",163);
		window.put("Strawberry",0);
		window.put("SwedishLeaf",2);
		window.put("Symbols",31);
		window.put("synthetic_control",3);
		window.put("ToeSegmentation1",22);
		window.put("ToeSegmentation2",17);
		window.put("Trace",8);
		window.put("TwoLeadECG",4);
		window.put("Two_Patterns",5);
		window.put("UWaveGestureLibraryAll",37);
		window.put("uWaveGestureLibrary_X",12);
		window.put("uWaveGestureLibrary_Y",12);
		window.put("uWaveGestureLibrary_Z",18);
		window.put("wafer",1);
		window.put("Wine",0);
		window.put("WordsSynonyms",21);
		window.put("Worms",27);
		window.put("WormsTwoClass",81);
		window.put("yoga",8);
	}

	public static int getWindow(String data) {
		if (window.size()==0) {
		    initUCRInfo();
		}
		if (window.containsKey(data))
			return window.get(data);
		else
			return 0;
	}
	
private static HashMap<String, Integer> window = new HashMap<String, Integer>();
}
