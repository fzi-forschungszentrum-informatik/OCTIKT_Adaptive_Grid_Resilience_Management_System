/*
 * This program is free software: you can redistribute it and/or modify it under the terms 
 * of the GNU General Public License as published by the Free Software Foundation, either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with this program. 
 * If not, see<https://www.gnu.org/licenses/>. 
*/

package unreleasedPackage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import UnreleasedClass;
import UnreleasedClass;
import UnreleasedClass;
import UnreleasedClass;
import UnreleasedClass;
import UnreleasedClass;
import UnreleasedClass;
import UnreleasedClass;
import UnreleasedClass;
import UnreleasedClass;
import UnreleasedClass;
import UnreleasedClass;
import UnreleasedClass;
import UnreleasedClass;
import UnreleasedClass;
import UnreleasedClass;
import UnreleasedClass;
import UnreleasedClass;
import UnreleasedClass;
import UnreleasedClass;
import UnreleasedClass;
import UnreleasedClass;
import UnreleasedClass;
import UnreleasedClass;

/**
 * 
 * @author Mischa Ahrens, Philip Ochs
 *
 */
// Every building has its own GridResilienceManagementModule
public class GridResilienceManagementModule extends UnreleasedClass {

	// Aggregated data of all involved buildings
	private Map<Integer, UnreleasedClass> buildingStateExchangeMap = new HashMap<Integer, UnreleasedClass>();
	// Sorted active power flexibilities of all relevant buildings
	private Map<Integer, Integer> currentSortedBuildingFlexibilities;
	// PV curtailment targets of all involved buildings
	private Map<Integer, Double> apparentPowerCurtailmentTargets = new HashMap<>();

	private Map<Integer, Double> apparentPowerCurtailmentSettingsForCurrent = new HashMap<>();
	// Map that contains the estimates by all involved buildings of the ability of
	// the DSO to communicate
	private Map<Integer, Boolean> previousCommunicationToDSOActiveValues = new HashMap<Integer, Boolean>();

	// Voltage progression of the most critical voltages in the perimeter of this
	// building
	private List<Double> lastTenMinutesOfMostCriticalPerimeterVoltages = new ArrayList<Double>();
	// Line current progression of the most critical lines in the grid branch of
	// this building
	private List<Boolean> lastXMinutesOfMaxLineCurrents = new ArrayList<Boolean>();
	// Progression of the line or trafo criticality the grid branch of this building
	private List<Boolean> lastXMinutesOfLineOrTrafoCriticality = new ArrayList<Boolean>();
	// Progression of the voltage criticality criteria used for PV curtailment, this
	// is different from lastTenMinutesOfMostCriticalPerimeterVoltages, since this
	// also contains voltage jumps
	private List<Boolean> lastXMinutesOfCriticalVoltagesForCurtailment = new ArrayList<Boolean>();
	// Active and reactive power values of all involved buildings
	private ArrayList<Double> pPerimeterValues = new ArrayList<>();
	private ArrayList<Double> qPerimeterValues = new ArrayList<>();
	// Last grid status that was received by UnreleasedClass
	private UnreleasedClass latestGridStatus;
	// Last grid status that was calculated by this building
	private UnreleasedClass latestCalculatedGridStatus;
	// Last transformer data received by the transformer
	private UnreleasedClass latestTransformerStatus;
	// Grid simulation
	private UnreleasedClass gridSimulation;
	// Calculates the buildings influence on critical situations in its perimeter
	private UnreleasedClass hpic;
	// UUID of the UnreleasedClass
	private UUID pvControllerUUID;

	// Determines if active power flexibility should be used
	private boolean usePTargets;
	// Determines if reactive power flexibility should be used
	private boolean useQTargets;
	// Use a Q(U) curve for voltage maintenance
	private boolean useQUCurve = false;
	// Use a P(U) curve for voltage maintenance
	private boolean usePUCurve = false;
	// Is set to true if a new grid status was received by UnreleasedClass
	private boolean newGridStatusReceived = false;
	// Is set to true if new transformer data was received
	private boolean newTransformerStatusReceived = false;
	// Is true as long as messages and data are received by other buildings
	private boolean buildingCommunicationActive = true;
	// Is true as long as messages and data are received by the transformer
	private boolean transformerCommunicationActive = true;
	// Is true if the transformer is not a VRDT or if a VRDT gives the building
	// clearance to act on critical voltages
	private boolean buildingClearance = false;
	// Is true if the building currently provides grid supportive active power
	// flexibility
	private boolean activePowerTargetActive = false;
	// Is true if the building has recently calculate its active power flexibility
	// but the flexibility is not yet called
	private boolean calculatedFlexibility = false;
	// Is true as long as the DSO communicates
	private boolean communicationToDSOActive = true;
	// Is true if the building currently compensates its own reactive power
	private boolean reactivePowerCompensationActive = false;
	// Is true if PV curtailment can be used in critical situations
	private boolean useApparentPowerCurtailment = true;

	// The current reactive power target, can take values from -1.0 to 1.0, is
	// multiplied with the maximum reactive power the building can produce or
	// consume
	private double reactivePowerTarget = 0.0;
	// The target that is used to provide active power flexibility in critical
	// situation, 1.0 if no target is currently used, 0.0 if a target is used
	private double activePowerTarget = 1.0;
	// Used for the Q(U) curve with hysteresis
	private double previousQUTarget = 0;
	// Voltage at the building's grid connection
	private double uBus;
	// Active power at the building's grid connection
	private double pBus;
	// Reactive power at the building's grid connection
	private double qBus;
	// The amount of energy flexibility that is currently available to approximate
	// the active
	// power target
	private double currentFlexibility = 0.0;
	// Current multiplier for a VRDT, this varies depending on the current VRDT
	// setting
	private double tVRDT = 1.0;
	// Last saved curtailment multiplier that led a critical line current to become
	// uncritical
	private double apparentPowerCurtailmentSettingForCurrent = 1.0;
	// Last saved curtailment multiplier that led a node voltage to become
	// uncritical
	private double apparentPowerCurtailmentSettingForVoltage = 1.0;
	// Last saved curtailment multiplier that the transformer to become uncritical
	private double apparentPowerCurtailmentSettingForTrafoTemp = 1.0;
	// Apparent power curtailment target, can take values between 0.0 and 1.0, is
	// multiplied with the maximum apparent power of the PV inverter
	private double apparentPowerCurtailmentTarget = -2.0;
	// Last communicated transformer temperature
	private double lastTransformerTemp;
	// Voltage on the higher voltage side of the transformer
	private double uMV = 1.0;
	// Last calculated maximum line current in the grid branch of this building
	protected double lastILineMax;

	// The last time the active power flexibility of this building was calculated
	private long lastTimeBuildingFlexibilityCalculated;
	// The last time a message was received from the DSO
	private long lastTimeDSOMessageReceived = 0;
	// The amount of time until a new active power flexibility can be triggered
	// after a previous one
	private long delayBetweenFlexibilityCalulations = 15 * 60;
	// The last time aggregated data of other buildings was received
	private long lastTimeBuildingASEReceived = 0;
	// The point in time when the highest/lowest voltage in the perimeter of the
	// building first became critical
	private long firstTimeVoltageWasCritical = Long.MAX_VALUE - 10000000;
	// The point in time when the highest/lowest voltage in the perimeter of the
	// building first became uncritical again
	private long firstTimeVoltageWasUncritical = Long.MAX_VALUE - 10000000;
	// The point in time when the highest current in the grid branch of the building
	// first became critical
	private long firstTimeLineWasCritical = Long.MAX_VALUE - 10000000;
	// The point in time when the highest current in the grid branch of the building
	// first became uncritical again
	private long firstTimeLineWasUncritical = Long.MAX_VALUE - 10000000;
	// The point in time when the highest current in the grid branch of the building
	// first became highly critical
	private long firstTimeLineWasHighlyCritical = Long.MAX_VALUE - 10000000;
	// The point in time when either the transformer temperature or the highest
	// current in the grid branch of the building first became critical
	private long firstTimeLineOrTrafoWasCritical = Long.MAX_VALUE - 10000000;
	// The point in time when the transformer temperature first became highly
	// critical
	private long firstTimeTrafoWasHighlyCritical = Long.MAX_VALUE - 10000000;
	// The point in time when the transformer temperature first stopped to be highly
	// critical
	private long firstTimeTrafoWasNotHighlyCritical = Long.MAX_VALUE - 10000000;
	// The interval in after which the grid simulation by the building is updated in
	// seconds
	private long gridSimulationUpdateInterval = 60;
	// The last time reactive power compensation was activated
	private long lastTimeReactivePowerCompensationActivated = 0;
	// The wall clock time at the start of the simulation
	private long timeAtStart;
	// The last point in time when new data was received from the transformer
	private long lastTimeTransformerStatusReceived = 0;

	// The optimization horizon for the optimization that tries to minimize the
	// distance to the active power target
	private int scheduleHorizon = (int) (3600 * 7);
	// The time resolution for the optimization
	private int resolution = 5; // 5 min
	// The bus ID of this building
	private int busID;
	// The bus ID of the most critical bus in the perimeter of this building
	private int uMostCriticalPerimeterID;
	// The duration for which the reactive power compensation is performed
	private int reactivePowerCompensationDuration = 8 * 60 * 60;
	// This describes the criticality of the transformer, different temperature lead
	// to different criticality levels
	private int trafoCriticality;
	// This describes the criticality of the most critical line in the grid branch
	// of this building, different currents or current jumps lead to different
	// criticality levels
	private int lineCriticality = 0;
	// The amount of time (in minutes) until counter measures can be scaled back
	// after a critical line current in this buildings grid branch was detected
	private int numberOfMinutesCriticalCurrent = 30;
	// The amount of time (in minutes) until counter measures can be scaled back
	// after a critical line current in this buildings grid branch or a critical
	// transformer was detected
	private int numberOfMinutesLineOrTrafoCriticality = 30;
	// The amount of time (in minutes) until counter measures can be scaled back
	// after a critical node voltage in this buildings perimeter was detected
	private int numberOfMinutesCriticalVoltage = 90;
	// This counter is used to indicate whether the calculated grid status is still
	// reliable
	private int voltageDeviationsCounter = 0;
	// This counter is used to indicate whether the calculated grid status is still
	// reliable
	private int voltageNonDeviationsCounter = 0;
	// This indicates the position of this building in its grid branch relative to
	// transformer
	private int gridPosition = -1;
	// This is the position of the building that is positioned furthest from the
	// transformer in the entire grid
	private int deepestGridPosition = -1;

	// This is the current message the building sends to other buildings, the
	// transformer and the DSO
	private String currentBuildingMessage;
	// This is the message the building sends to other buildings, the transformer
	// and the DSO
	private String buildingMessage;

	// If this criterion becomes true, this serves as an indicator that active power
	// flexibility might be needed to counteract critical currents or critical
	// transformer temperatures
	private boolean lineAndTrafoCriticalityCriterionForActiveTargets;
	// If this criterion becomes true, this serves as an indicator that active power
	// flexibility might be needed to counteract critical node voltages
	private boolean voltageCriticalityCriterionForActiveTargets;
	// If this criterion becomes true, this serves as an indicator that apparent
	// power curtailment might be needed to counteract critical transformer
	// temperatures
	private boolean trafoCriticalityCriterionForCurtailment;
	// If this criterion becomes true, this serves as an indicator that apparent
	// power curtailment might be needed to counteract critical line currents
	private boolean lineCriticalityCriterionForCurtailment;
	// If this criterion becomes true, this serves as an indicator that apparent
	// power curtailment might be needed to counteract critical node voltages
	private boolean voltageCriticalityCriterionForCurtailment;
	// If this criterion becomes true, this serves as an indicator that reactive
	// power compensation might be needed to counteract critical currents or
	// critical transformer temperatures
	private boolean criticalityCriterionForReactiveCompensation;
	// If this criterion becomes true, this serves as an indicator that voltage
	// induced curtailment can be scaled back
	private boolean voltageCriticalityCriterionForCurtailmentLowering;
	// If this criterion becomes true, this serves as an indicator that line current
	// induced curtailment can be scaled back
	private boolean lineCriticalityCriterionForCurtailmentLowering;
	// If this criterion becomes true, this serves as an indicator that transformer
	// temperature induced curtailment can be scaled back
	private boolean trafoCriticalityCriterionForCurtailmentLowering;
	// This indicates new aggregated building data was received by this building
	private boolean newBuildingASEReceived = true;
	// This indicates if the currently calculated grid status is reliable
	private boolean gridSimulationReliable = true;

	public GridResilienceManagementModule(UnreleasedClass data) {
		super(data);
	}

	@Override
	public void UnreleasedClass throws UnreleasedClass
	{

		// Subscribe to OCRegistry to receive grid, transformer and building data
		unreleasedMethod.unreleasedMethod.unreleasedMethod(UnreleasedClass.class, this);
		unreleasedMethod.unreleasedMethod.unreleasedMethod(UnreleasedClass.class, this);
		unreleasedMethod.unreleasedMethod.unreleasedMethod(UnreleasedClass.class, this);
		unreleasedMethod.unreleasedMethod.unreleasedMethod(UnreleasedClass.class, this);

		// Extract building/bus ID from UUID
		String uuid = unreleasedMethod.unreleasedMethod.unreleasedMethod.toString();
		uuid = uuid.substring(uuid.length() - 4);
		busID = Integer.parseInt(uuid) - 1; // Bus number of this building in the given grid
		uMostCriticalPerimeterID = busID;
		unreleasedMethod.setBusID(busID);

		// Initialize the first building message, this type of message is send to other
		// buildings, the DSO and the transformer
		currentBuildingMessage = "activePowerTarget;" + activePowerTargetActive + ";" + activePowerTarget
				+ ";calculatedFlexibility;" + calculatedFlexibility + ";currentFlexibility;" + currentFlexibility
				+ ";reactivePowerTarget;" + reactivePowerTarget + ";reactivePowerCompensationActive;"
				+ reactivePowerCompensationActive + ";communicationToDSOActive;" + communicationToDSOActive
				+ ";apparentPowerCurtailmentTarget;" + apparentPowerCurtailmentTarget + ";gridSimulationReliable;"
				+ gridSimulationReliable + ";apparentPowerCurtailmentSettingForCurrent;"
				+ apparentPowerCurtailmentSettingForCurrent;

		// Determine which counter measures to critical situations can be used
		usePTargets = unreleasedMethod.unreleasedMethod;

		useQTargets = unreleasedMethod.unreleasedMethod;

		useApparentPowerCurtailment = unreleasedMethod.unreleasedMethod;

		// The grid simulation interval is the time between two consecutive grid status
		// calculations
		this.gridSimulationUpdateInterval = unreleasedMethod.unreleasedMethod;

		timeAtStart = unreleasedMethod.unreleasedMethod;
	}

	@Override
	public void unreleasedMethod throws UnreleasedClass
	{
		super.unreleasedMethod;

		long now = unreleasedMethod.unreleasedMethod;

		if (pvControllerUUID == null)
			pvControllerUUID = unreleasedMethod.unreleasedMethod.unreleasedMethod(UnreleasedClass.class,
					UnreleasedClass.class);

		// Otherwise the building thinks that communication is down at the beginning of
		// the simulation
		if (lastTimeBuildingASEReceived == 0)

			lastTimeBuildingASEReceived = now + 5;

		// If no building data was received for over a minute, assume that the
		// communication to other buildings is disturbed
		if (lastTimeBuildingASEReceived + 60 < now && buildingCommunicationActive) {

			buildingCommunicationActive = false;

			unreleasedMethod.unreleasedMethod.unreleasedMethod("Building " + ((Integer) (busID + 1)).toString()
					+ ": Communication to other buildings is probably disturbed.");
		}

		// Otherwise the building thinks that communication is down at the beginning of
		// the simulation
		if (lastTimeTransformerStatusReceived == 0)

			lastTimeTransformerStatusReceived = now + 5;

		// If no transformer data was received for over a minute, assume that the
		// communication to the transformer is disturbed
		if (transformerCommunicationActive && lastTimeTransformerStatusReceived + 60 < now) {

			tVRDT = 1.0;
			buildingClearance = true;

			transformerCommunicationActive = false;

			unreleasedMethod.unreleasedMethod.unreleasedMethod("Building " + ((Integer) (busID + 1)).toString()
					+ ": Communication to trafo is probably disturbed.");
		}

		// Only do something if new local grid data was received by
		// UnreleasedClass and only do something if new transformer data was
		// received or if the communication to other buildings or the transformer is
		// disturbed
		if (newGridStatusReceived && (newTransformerStatusReceived || !buildingCommunicationActive
				|| !transformerCommunicationActive)) {

			newGridStatusReceived = false;
			newTransformerStatusReceived = false;

			// If the communication to the DSO is currently assumed to be disturbed, but a
			// new DSO message was received, assume that the communication to the DSO is
			// online again
			if (lastTimeDSOMessageReceived != unreleasedMethod.unreleasedMethod) {

				lastTimeDSOMessageReceived = unreleasedMethod.unreleasedMethod;

				if (!communicationToDSOActive) {

					unreleasedMethod.unreleasedMethod
							.unreleasedMethod("Building " + ((Integer) (busID + 1)).toString() + ": DSO communicates again");

					communicationToDSOActive = true;
				}

				// Reset all communication disturbance indicators to default values
				firstTimeVoltageWasCritical = Long.MAX_VALUE - 10000000;
				firstTimeVoltageWasUncritical = Long.MAX_VALUE - 10000000;

				firstTimeLineWasCritical = Long.MAX_VALUE - 10000000;
				firstTimeLineWasUncritical = Long.MAX_VALUE - 10000000;

				firstTimeLineOrTrafoWasCritical = Long.MAX_VALUE - 10000000;

				firstTimeTrafoWasHighlyCritical = Long.MAX_VALUE - 10000000;
				firstTimeTrafoWasNotHighlyCritical = Long.MAX_VALUE - 10000000;

				firstTimeLineWasHighlyCritical = Long.MAX_VALUE - 10000000;

				lineCriticality = 0;
			}

			// If the following class variables differ from the ones present in
			// UnreleasedClass, e.g. because new target where received by the
			// DSO, replace the values of the class variables by the ones in
			// UnreleasedClass
			if (apparentPowerCurtailmentTarget != unreleasedMethod.unreleasedMethod) {

				apparentPowerCurtailmentTarget = unreleasedMethod.unreleasedMethod;
			}

			if (apparentPowerCurtailmentSettingForCurrent != unreleasedMethod.unreleasedMethod) {

				apparentPowerCurtailmentSettingForCurrent = unreleasedMethod.unreleasedMethod;
			}

			if (apparentPowerCurtailmentSettingForTrafoTemp != unreleasedMethod
					.unreleasedMethod) {

				apparentPowerCurtailmentSettingForTrafoTemp = unreleasedMethod
						.unreleasedMethod;
			}

			if (apparentPowerCurtailmentSettingForVoltage != unreleasedMethod.unreleasedMethod) {

				apparentPowerCurtailmentSettingForVoltage = unreleasedMethod.unreleasedMethod;
			}

			if (!calculatedFlexibility && unreleasedMethod.unreleasedMethod) {

				calculatedFlexibility = unreleasedMethod.unreleasedMethod;
				currentFlexibility = unreleasedMethod.unreleasedMethod;
			}

			if (!activePowerTargetActive && unreleasedMethod.unreleasedMethod) {

				activePowerTargetActive = true;
				activePowerTarget = unreleasedMethod.unreleasedMethod;

				calculatedFlexibility = false;
				currentFlexibility = 0.0;
			}

			if (reactivePowerTarget != unreleasedMethod.unreleasedMethod) {

				reactivePowerTarget = unreleasedMethod.unreleasedMethod;

				if (reactivePowerTarget != 0.0) {

					reactivePowerCompensationActive = false;
					unreleasedMethod.unreleasedMethod(false);
				}
			}

			if (activePowerTarget != unreleasedMethod.unreleasedMethod) {

				activePowerTarget = unreleasedMethod.unreleasedMethod;
			}

			if (reactivePowerCompensationActive != unreleasedMethod.unreleasedMethod) {

				reactivePowerCompensationActive = unreleasedMethod.unreleasedMethod;

				if (reactivePowerCompensationActive) {
					lastTimeReactivePowerCompensationActivated = now;
				}
			}

			// If an active power target is currently used, but non-existent or the maximum
			// amount of time the target is upheld has ended, reset active power target
			// variables to their default values
			if (activePowerTargetActive && (unreleasedMethod.unreleasedMethod == null
					|| unreleasedMethod.unreleasedMethod.unreleasedMethod <= now)) {

				activePowerTargetActive = false;
				activePowerTarget = 1.0;
				unreleasedMethod.unreleasedMethod(activePowerTargetActive);
				unreleasedMethod.unreleasedMethod(activePowerTarget);
			}

			// If reactive power compensation is currently used, but the time limit for
			// reactive power compensation is reached, disable reactive power compensation
			if ((reactivePowerCompensationActive || unreleasedMethod.unreleasedMethod)
					&& lastTimeReactivePowerCompensationActivated + reactivePowerCompensationDuration < now) {

				reactivePowerCompensationActive = false;

				unreleasedMethod.unreleasedMethod(false);

				// Send signal to stop reactive power compensation to UnreleasedClass
				UnreleasedClass pvExchange = new UnreleasedClass(this.unreleasedMethod, pvControllerUUID,
						unreleasedMethod.unreleasedMethod, reactivePowerCompensationActive, reactivePowerTarget,
						reactivePowerCompensationDuration, apparentPowerCurtailmentTarget);

				unreleasedMethod.unreleasedMethod.unreleasedMethod(UnreleasedClass.class, pvExchange);

				unreleasedMethod.unreleasedMethod.unreleasedMethod("Building " + ((Integer) (busID + 1)).toString()
						+ " is stopping to compensate reactive power due to the time limit being reached");
			}

			// If the current active power flexibility of the building was calculated, but
			// the delay between calculations has been exceeded, set the corresponding
			// variables to their default values
			if (calculatedFlexibility
					&& now - lastTimeBuildingFlexibilityCalculated > delayBetweenFlexibilityCalulations) {

				calculatedFlexibility = false;
				currentFlexibility = 0.0;
				unreleasedMethod.unreleasedMethod(calculatedFlexibility);
				unreleasedMethod.unreleasedMethod(currentFlexibility);
			}

			// Only do something, if at least one of the implemented counter measures to
			// critical situations can be used
			if (usePTargets || useQTargets || useApparentPowerCurtailment) {
				// Only calculate a new grid status, if the other buildings communicate
				if (buildingCommunicationActive && newBuildingASEReceived && !pPerimeterValues.isEmpty()
						&& !qPerimeterValues.isEmpty()) {

					newBuildingASEReceived = false;

					// Calculate the current grid status from own data, data received by other
					// buildings and the transformer
					try {

						if (gridSimulation == null) {

							UnreleasedClass grid = latestGridStatus.unreleasedMethod.unreleasedMethod;

							gridSimulation = new UnreleasedClass(grid, new UnreleasedClass(grid),
									latestGridStatus.unreleasedMethod);

						}

						gridSimulation.unreleasedMethod(pPerimeterValues, qPerimeterValues, tVRDT, now);
						gridSimulation.unreleasedMethod.unreleasedMethod(uMV);

						latestCalculatedGridStatus = new UnreleasedClass(gridSimulation.unreleasedMethod,
								gridSimulation.unreleasedMethod, gridSimulation.unreleasedMethod, // Real part of the Line
								// currents
								gridSimulation.unreleasedMethod, // Reactive part of the Line currents
								gridSimulation.unreleasedMethod, gridSimulation.unreleasedMethod,
								gridSimulation.unreleasedMethod, gridSimulation.unreleasedMethod,
								gridSimulation.unreleasedMethod, // modified active buspower
								gridSimulation.unreleasedMethod // modified reactive buspower
						);
					} catch (UnreleasedClass e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// If the calculated local voltage deviates more than 0.005 pu from the locally
					// measured voltage for two time steps/measurement intervals, the grid
					// simulation data is assumed to be unreliable
					if (timeAtStart + 2 * 60 < now
							&& Math.abs(uBus - latestCalculatedGridStatus.unreleasedMethod.get(busID)) > 0.005
							&& (transformerCommunicationActive
									|| lastTimeTransformerStatusReceived + 4 * 60 < now)) {

						voltageNonDeviationsCounter = 0;
						voltageDeviationsCounter++;

						if (voltageDeviationsCounter > 1) {

							gridSimulationReliable = false;
							voltageDeviationsCounter = 0;
						}
					} else {

						voltageDeviationsCounter = 0;
						voltageNonDeviationsCounter++;

						if (voltageNonDeviationsCounter > 1) {

							gridSimulationReliable = true;
							voltageNonDeviationsCounter = 0;
						}
					}

					// Only do collaborative resilience management with other buildings, if the
					// current grid simulation is reliable, otherwise to local resilience management
					if (gridSimulationReliable)
						implementDistributedGridResilienceManagement();
					else
						implementDecentralizedGridResilienceManagement(getLocalBusVoltage(latestGridStatus));

				} else if (!buildingCommunicationActive) {
					// If the communication is completely interrupted, the building has to act on
					// its own
					implementDecentralizedGridResilienceManagement(getLocalBusVoltage(latestGridStatus));
				}
			}

			// Communicate the current situation at your own bus to other buildings, the
			// DSO and the transformer
			currentBuildingMessage = "activePowerTarget;" + activePowerTargetActive + ";" + activePowerTarget
					+ ";calculatedFlexibility;" + calculatedFlexibility + ";currentFlexibility;" + currentFlexibility
					+ ";reactivePowerTarget;" + reactivePowerTarget + ";reactivePowerCompensationActive;"
					+ reactivePowerCompensationActive + ";communicationToDSOActive;" + communicationToDSOActive
					+ ";apparentPowerCurtailmentTarget;" + apparentPowerCurtailmentTarget + ";gridSimulationReliable;"
					+ gridSimulationReliable + ";apparentPowerCurtailmentSettingForCurrent;"
					+ apparentPowerCurtailmentSettingForCurrent;

			// Send new message to other buildings, the transformer and the DSO, if
			// something has changed
			if (pBus != Math.round(getLocalBusActivePower(latestGridStatus))
					|| qBus != Math.round(getLocalBusReactivePower(latestGridStatus))
					|| uBus != Math.round(getLocalBusVoltage(latestGridStatus) * 1000d) / 1000d
					|| !currentBuildingMessage.equals(buildingMessage)) {

				uBus = getLocalBusVoltage(latestGridStatus);
				pBus = Math.round(getLocalBusActivePower(latestGridStatus));
				qBus = Math.round(getLocalBusReactivePower(latestGridStatus));
				buildingMessage = currentBuildingMessage;

				UnreleasedClass bse = new UnreleasedClass(unreleasedMethod, now);

				bse.unreleasedMethod(currentBuildingMessage);
				bse.unreleasedMethod(busID);
				bse.unreleasedMethod(uBus);
				bse.unreleasedMethod(getLocalBusActivePower(latestGridStatus));
				bse.unreleasedMethod(getLocalBusReactivePower(latestGridStatus));
				bse.unreleasedMethod(unreleasedMethod.unreleasedMethod.unreleasedMethod);

				unreleasedMethod.unreleasedMethod.unreleasedMethod(UnreleasedClass.class, this, bse);
			}
		}
	}

	@Override
	public <T extends UnreleasedClass> void unreleasedMethod(Class<T> type, T event) throws UnreleasedClass {
		if (event instanceof UnreleasedClass) {
			UnreleasedClass exsc = (UnreleasedClass) event;

			// Receive grid data from UnreleasedClass via
			// UnreleasedClass, UnreleasedClass and
			// UnreleasedClass
			if (exsc.unreleasedMethod.equals(UnreleasedClass.class)) {

				newGridStatusReceived = true;
				UnreleasedClass gse = unreleasedMethod.unreleasedMethod.unreleasedMethod(UnreleasedClass.class,
						exsc.unreleasedMethod);

				latestGridStatus = gse.unreleasedMethod(); // Get grid status

				// Initialize the active and reactive power values in the grid
				if (pPerimeterValues.isEmpty() && qPerimeterValues.isEmpty()) {

					for (int i = 0; i < latestGridStatus.unreleasedMethod.unreleasedMethod(); i++) {

						pPerimeterValues.add(0.0);
						qPerimeterValues.add(0.0);
					}
				}

				// Find the position of the building in the grid relative to the
				// transformer from the line configuration
				if (gridPosition == -1) {

					int distanceToSlack = busID + 1;

					while (distanceToSlack != 1) {

						for (UnreleasedClass line : latestGridStatus.unreleasedMethod.unreleasedMethod) {

							if (Integer.valueOf(line.unreleasedMethod.substring(1)) == distanceToSlack) {

								distanceToSlack = Integer.valueOf(line.unreleasedMethod.substring(1));
								gridPosition++;
							}
						}
					}
				}

				// Find the position of the building furthest away from the transformer in the
				// entire grid from the line configuration
				if (deepestGridPosition == -1) {

					for (int iD = 2; iD < latestGridStatus.unreleasedMethod.size(); iD++) {

						int gridPosition = -1;

						int distanceToSlack = iD + 1;

						while (distanceToSlack != 1) {

							for (UnreleasedClass line : latestGridStatus.unreleasedMethod.unreleasedMethod) {

								if (Integer.valueOf(line.unreleasedMethod.substring(1)) == distanceToSlack) {

									distanceToSlack = Integer.valueOf(line.unreleasedMethod.substring(1));
									gridPosition++;
								}
							}
						}

						if (gridPosition > deepestGridPosition)
							deepestGridPosition = gridPosition;
					}
				}

				newGridStatusReceived = true;
			}
			// Receive transformer data from UnreleasedClass via
			// UnreleasedClass, UnreleasedClass and
			// UnreleasedClass
			else if (exsc.unreleasedMethod.equals(UnreleasedClass.class)) {

				UnreleasedClass rse = unreleasedMethod.unreleasedMethod.unreleasedMethod(UnreleasedClass.class,
						exsc.unreleasedMethod);

				latestTransformerStatus = rse;
				newTransformerStatusReceived = true;
				transformerCommunicationActive = true;
				lastTimeTransformerStatusReceived = unreleasedMethod.unreleasedMethod;
			}
			// Receive clearance data from UnreleasedClass via
			// UnreleasedClass, UnreleasedClass and
			// UnreleasedClass
			else if (exsc.unreleasedMethod.equals(UnreleasedClass.class)) {

				UnreleasedClass rmse = unreleasedMethod.unreleasedMethod.unreleasedMethod(UnreleasedClass.class,
						exsc.unreleasedMethod);

				this.buildingClearance = rmse.unreleasedMethod;
				this.tVRDT = rmse.unreleasedMethod;
				this.uMV = rmse.unreleasedMethod;
			}
			// Receive aggregated messages and data of other buildings from
			// UnreleasedClass, UnreleasedClass and
			// UnreleasedClass
			else if (exsc.unreleasedMethod.equals(UnreleasedClass.class)) {

				UnreleasedClass base = unreleasedMethod.unreleasedMethod.unreleasedMethod(UnreleasedClass.class,
						exsc.unreleasedMethod);

				buildingStateExchangeMap = base.unreleasedMethod;

				if (!buildingCommunicationActive)
					unreleasedMethod.unreleasedMethod.unreleasedMethod("Building " + ((Integer) (busID + 1)).toString()
							+ ": Communication to other buildings is up again.");

				buildingCommunicationActive = true;
				lastTimeBuildingASEReceived = unreleasedMethod.unreleasedMethod;
				newBuildingASEReceived = true;

				for (Map.Entry<Integer, UnreleasedClass> entry : buildingStateExchangeMap.entrySet()) {

					UnreleasedClass entryValue = entry.getValue();

					if (entryValue != null) {

						int idBus = entryValue.unreleasedMethod;

						pPerimeterValues.set(idBus, -entryValue.unreleasedMethod);
						qPerimeterValues.set(idBus, -entryValue.unreleasedMethod);
						apparentPowerCurtailmentTargets.put(idBus,
								Double.valueOf(entryValue.unreleasedMethod.split(";")[14]));
						apparentPowerCurtailmentSettingsForCurrent.put(idBus,
								Double.valueOf(entryValue.unreleasedMethod.split(";")[18]));

						// If any other building says the communication to the DSO is disturbed, the
						// communication to the DSO is assumed to be disturbed. If any other building
						// says the communication to the DSO is online if it previously said that it was
						// disturbed, assume that the communication to the DSO is online.
						if (communicationToDSOActive && !Boolean.valueOf(entryValue.unreleasedMethod.split(";")[12])
								&& previousCommunicationToDSOActiveValues.get(idBus) != null
								&& previousCommunicationToDSOActiveValues.get(idBus))

							communicationToDSOActive = false;

						else if (!communicationToDSOActive
								&& Boolean.valueOf(entryValue.unreleasedMethod.split(";")[12])
								&& previousCommunicationToDSOActiveValues.get(idBus) != null
								&& !previousCommunicationToDSOActiveValues.get(idBus))

							communicationToDSOActive = true;

						previousCommunicationToDSOActiveValues.put(idBus,
								Boolean.valueOf(entryValue.unreleasedMethod.split(";")[12]));
					}
				}
			}
		}
	}

	// Method that implements counter measures against locally determined (from
	// voltages) critical situations when communication between buildings and/or to
	// the DSO or the transformer is disturbed or if the grid simulation is assumed
	// to be unreliable
	public void implementDecentralizedGridResilienceManagement(double uBus) {

		double currentQUTarget = 0.0;
		double currentReactivePowerTarget = reactivePowerTarget;

		// Save the new voltage and remove the oldest one from a list that holds ten
		// voltages
		if (lastTenMinutesOfMostCriticalPerimeterVoltages.isEmpty()) {

			for (int i = (int) (10 * 60 / gridSimulationUpdateInterval - 1); i >= 0; i--) {

				lastTenMinutesOfMostCriticalPerimeterVoltages.add(uBus);
			}
		} else {

			lastTenMinutesOfMostCriticalPerimeterVoltages.add(uBus);
			lastTenMinutesOfMostCriticalPerimeterVoltages.remove(0);
		}

		boolean uMaxNodeLowEnough = true;
		boolean uMaxNodeHighEnough = true;

		// Determine if the voltages have been low enough or high enough to reduce
		// reactive power feed-in or draw
		for (int i = (int) (10 * 60 / gridSimulationUpdateInterval - 1); i >= 0; i--) {

			if (lastTenMinutesOfMostCriticalPerimeterVoltages.get(i) >= 1.065) {

				uMaxNodeLowEnough = false;
			}

			if (lastTenMinutesOfMostCriticalPerimeterVoltages.get(i) <= 0.925) {

				uMaxNodeHighEnough = false;
			}
		}

		// Determine the necessity to curtail the apparent power at the grid connection
		// by curtailing PV generation from the locally measured voltage and the
		// relative position of the building in the grid
		boolean apparentPowerCurtailmentCriterion = uBus > 1.01 + 0.04 * gridPosition / deepestGridPosition
				|| reactivePowerTarget > 0.0 && uBus > 1.01 + 0.03 * gridPosition / deepestGridPosition;

		// Determine the necessity to use active power flexibility from the locally
		// measured voltage and the relative position of the building in the grid
		boolean activePowerFlexibilityCriterion = uBus > 1.01 + 0.04 * gridPosition / deepestGridPosition
				|| reactivePowerTarget > 0.0 && uBus > 1.01 + 0.03 * gridPosition / deepestGridPosition
				|| uBus < 0.99 - 0.04 * gridPosition / deepestGridPosition
				|| reactivePowerTarget < 0.0 && uBus < 0.99 - 0.03 * gridPosition / deepestGridPosition
				|| apparentPowerCurtailmentTarget != -2.0;

		// Save the new voltage and remove the oldest one from a list that holds X
		// voltages
		if (lastXMinutesOfCriticalVoltagesForCurtailment.isEmpty()) {

			for (int i = (int) (numberOfMinutesCriticalVoltage * 60 / gridSimulationUpdateInterval - 1); i >= 0; i--) {
				lastXMinutesOfCriticalVoltagesForCurtailment.add(apparentPowerCurtailmentCriterion);
			}
		} else {

			lastXMinutesOfCriticalVoltagesForCurtailment.add(apparentPowerCurtailmentCriterion);
			lastXMinutesOfCriticalVoltagesForCurtailment.remove(0);
		}

		// Determine if the apparent power curtailment criterion was low enough in
		// the last X minutes to reduce curtailment
		boolean apparentPowerCurtailmentCriterionLowEnough = !lastXMinutesOfCriticalVoltagesForCurtailment
				.contains(true);

		// Save the new active power flexibility criterion and remove the oldest one
		// from a list that holds X voltages
		if (lastXMinutesOfLineOrTrafoCriticality.isEmpty()) {

			for (int i = (int) (numberOfMinutesLineOrTrafoCriticality * 60 / gridSimulationUpdateInterval
					- 1); i >= 0; i--) {
				lastXMinutesOfLineOrTrafoCriticality.add(activePowerFlexibilityCriterion);
			}
		} else {

			lastXMinutesOfLineOrTrafoCriticality.add(activePowerFlexibilityCriterion);
			lastXMinutesOfLineOrTrafoCriticality.remove(0);
		}

		// Determine if the active power flexibility criterion was low enough in the
		// last X minutes to stop active power flexibility provision
		boolean activePowerFlexibilityCriterionLowEnough = !lastXMinutesOfLineOrTrafoCriticality.contains(true);

		// If Q targets can be used, reactive power compensation and reactive power
		// based voltage regulation can be used
		if (useQTargets) {
			// If a Q(U) curve should be used to address critical voltages
			if (useQUCurve)
				// compute currentQUTarget
				currentQUTarget = UnreleasedClass.unreleasedMethod.unreleasedMethod(this.previousQUTarget, uBus);

			// If no Q(U) curve should be used, use another voltage regulation algorithm
			else {
				// If no or positive reactive power is already used
				if (reactivePowerTarget >= 0.0) {

					// If the local voltage is very high or if the current reactive power target is
					// zero and there was a voltage jump
					if (uBus > 1.09 || lastTenMinutesOfMostCriticalPerimeterVoltages
							.get(lastTenMinutesOfMostCriticalPerimeterVoltages.size() - 1)
							- lastTenMinutesOfMostCriticalPerimeterVoltages
									.get(lastTenMinutesOfMostCriticalPerimeterVoltages.size() - 2) > 0.01
							&& lastTenMinutesOfMostCriticalPerimeterVoltages
									.get(lastTenMinutesOfMostCriticalPerimeterVoltages.size() - 2) > 1.05
							&& reactivePowerTarget == 0.0)

						currentReactivePowerTarget = reactivePowerTarget + 0.5;

					// If the local voltage is high and the current reactive power target is zero or
					// if the current reactive power target is below 0.5 and there was a voltage
					// jump
					else if (uBus > 1.07 && reactivePowerTarget == 0.0 || lastTenMinutesOfMostCriticalPerimeterVoltages
							.get(lastTenMinutesOfMostCriticalPerimeterVoltages.size() - 1)
							- lastTenMinutesOfMostCriticalPerimeterVoltages
									.get(lastTenMinutesOfMostCriticalPerimeterVoltages.size() - 2) > 0.01
							&& lastTenMinutesOfMostCriticalPerimeterVoltages
									.get(lastTenMinutesOfMostCriticalPerimeterVoltages.size() - 2) > 1.05
							&& reactivePowerTarget <= 0.5)

						currentReactivePowerTarget = reactivePowerTarget + 0.25;

					// If the local voltage is high
					else if (uBus > 1.07)

						currentReactivePowerTarget = reactivePowerTarget + 0.01;

					// If the local voltage was low enough for the last ten minutes, but the
					// reactive power target is positive
					else if ((uMaxNodeLowEnough || uBus < 1.0) && reactivePowerTarget > 0.0) {

						currentReactivePowerTarget = reactivePowerTarget - 0.01;

						// Correction if the reactive power target got smaller than 0.0
						if (currentReactivePowerTarget < 0.0)

							currentReactivePowerTarget = 0.0;
					}
					// Correction if the reactive power target got greater than 1.0
					if (currentReactivePowerTarget > 1.0)

						currentReactivePowerTarget = 1.0;
				}
				// If no or negative reactive power is already used
				if (reactivePowerTarget <= 0.0) {
					// If the local voltage is very low
					if (uBus < 0.91)

						currentReactivePowerTarget = reactivePowerTarget - 0.5;

					// If the local voltage is low and the current reactive power target is zero
					else if (uBus < 0.92 && reactivePowerTarget == 0.0)

						currentReactivePowerTarget = reactivePowerTarget - 0.25;

					// If the local voltage is low
					else if (uBus < 0.92)

						currentReactivePowerTarget = reactivePowerTarget - 0.01;

					// If the local voltage was high enough for the last ten minutes, but the
					// reactive power target is negative
					else if ((uMaxNodeHighEnough || uBus > 1.0) && reactivePowerTarget < 0.0) {

						currentReactivePowerTarget = reactivePowerTarget + 0.01;

						// Correction if the reactive power target got larger than zero
						if (currentReactivePowerTarget > 0.0)

							currentReactivePowerTarget = 0.0;
					}
					// Correction if the reactive power target got smaller than -1.0
					if (currentReactivePowerTarget < -1.0)

						currentReactivePowerTarget = -1.0;
				}
			}

			// Correction to prevent reactive power target steps smaller than 0.01
			currentReactivePowerTarget = Math.round(currentReactivePowerTarget * 100d) / 100d;

			// Check if critical situation was (one step before) or is (currently) there and
			// a Q(U) curve is used
			if (this.previousQUTarget != 0 || currentQUTarget != 0) {

				// Send command to UnreleasedClass for adjusting reactive power of inverter
				UnreleasedClass UnreleasedClass = new UnreleasedClass(this.unreleasedMethod, pvControllerUUID,
						unreleasedMethod.unreleasedMethod, currentQUTarget);
				unreleasedMethod.unreleasedMethod.unreleasedMethod(UnreleasedClass.class, UnreleasedClass);

				// Update previousQUTarget to use in the next step
				this.previousQUTarget = currentQUTarget;
			}

			// Check if the critical situation requires a new reactive power target
			if (currentReactivePowerTarget != reactivePowerTarget) {

				if (reactivePowerTarget == 0.0)

					unreleasedMethod.unreleasedMethod.unreleasedMethod("Building " + ((Integer) (busID + 1)).toString()
							+ " is implementing a reactive power target");

				else if (currentReactivePowerTarget == 0.0)

					unreleasedMethod.unreleasedMethod.unreleasedMethod("Building " + ((Integer) (busID + 1)).toString()
							+ " is not implementing a reactive power target anymore");

				reactivePowerTarget = currentReactivePowerTarget;
				unreleasedMethod.unreleasedMethod(reactivePowerTarget);
				reactivePowerCompensationActive = false;
				unreleasedMethod.unreleasedMethod(false);

				// Send command to UnreleasedClass for adjusting reactive power of inverter
				UnreleasedClass UnreleasedClass = new UnreleasedClass(this.unreleasedMethod, pvControllerUUID,
						unreleasedMethod.unreleasedMethod, reactivePowerCompensationActive, currentReactivePowerTarget,
						reactivePowerCompensationDuration, apparentPowerCurtailmentTarget);

				unreleasedMethod.unreleasedMethod.unreleasedMethod(UnreleasedClass.class, UnreleasedClass);
			}

			// Check if the critical situation requires reactive power compensation
			if (activePowerFlexibilityCriterion && currentReactivePowerTarget == 0.0
					&& !reactivePowerCompensationActive) {

				reactivePowerCompensationActive = true;
				unreleasedMethod.unreleasedMethod(true);

				// Send command to UnreleasedClass to adjust reactive power of inverter
				UnreleasedClass UnreleasedClass = new UnreleasedClass(this.unreleasedMethod, pvControllerUUID,
						unreleasedMethod.unreleasedMethod, reactivePowerCompensationActive, currentReactivePowerTarget,
						reactivePowerCompensationDuration, apparentPowerCurtailmentTarget);

				unreleasedMethod.unreleasedMethod.unreleasedMethod(UnreleasedClass.class, UnreleasedClass);

				unreleasedMethod.unreleasedMethod.unreleasedMethod(
						"Building " + ((Integer) (busID + 1)).toString() + " is compensating its reactive power");

				lastTimeReactivePowerCompensationActivated = unreleasedMethod.unreleasedMethod;
			}
		}

		// If P targets can be used, active power flexibility can be used
		if (usePTargets) {
			// If a P(U) curve should be used to address critical situations
			if (usePUCurve) {

				double currentPUTarget = UnreleasedClass.unreleasedMethod.unreleasedMethod(uBus);
				UnreleasedClass targetLoadProfile = new UnreleasedClass();

				// If the target load profile is null or expired
				if (unreleasedMethod.unreleasedMethod == null
						|| unreleasedMethod.unreleasedMethod.unreleasedMethod <= unreleasedMethod.unreleasedMethod) {

					if (currentPUTarget != 0) {
						// create all new profile
						targetLoadProfile.unreleasedMethod(UnreleasedClass.unreleasedField,
								unreleasedMethod.unreleasedMethod, (int) Math.round(currentPUTarget * 10000));
						targetLoadProfile.unreleasedMethod(unreleasedMethod.unreleasedMethod + scheduleHorizon);

					} else {
						// No critical state -> set null
						targetLoadProfile = null;
					}

					// If the target load profile is not expired
				} else {

					// Only update P(U) value of the target load profile
					long previousStartTime = unreleasedMethod.unreleasedMethod
							.getFloorEntry(UnreleasedClass.unreleasedField, unreleasedMethod.unreleasedMethod).getKey();
					targetLoadProfile.unreleasedMethod(UnreleasedClass.unreleasedField, previousStartTime,
							(int) Math.round(currentPUTarget * 10000));
					targetLoadProfile.unreleasedMethod(previousStartTime + scheduleHorizon);
				}

				// Set load profile
				unreleasedMethod.unreleasedMethod(targetLoadProfile);

				// If no P(U) curve should be used, use another active power flexibility
				// provision algorithm
			} else {

				int currentActivePowerTarget = 0;

				// If active power flexibility is needed and currently no active power target is
				// used and the currently usable active power flexibility has not been
				// calculated, calculate a new possible active power flexibility provision
				if (!activePowerTargetActive
						&& (!calculatedFlexibility
								|| lastTimeBuildingFlexibilityCalculated + 15 * 60 < unreleasedMethod.unreleasedMethod)
						&& activePowerFlexibilityCriterion && (unreleasedMethod.unreleasedMethod == null
								|| unreleasedMethod.unreleasedMethod.unreleasedMethod <= unreleasedMethod.unreleasedMethod)) {

					// Initialize new UnreleasedClass
					UnreleasedClass fd = new UnreleasedClass(unreleasedMethod.unreleasedMethod.unreleasedMethod);

					fd.unreleasedMethod(scheduleHorizon / 60);
					fd.unreleasedMethod(scheduleHorizon);
					fd.unreleasedMethod(resolution);
					fd.unreleasedMethod(unreleasedMethod.unreleasedMethod);
					fd.unreleasedMethod(currentActivePowerTarget);

					unreleasedMethod.unreleasedMethod(fd);

					// Tell UnreleasedClass to calculate the current active power
					// flexibility
					calculateFlexibility();

					currentFlexibility = unreleasedMethod.unreleasedMethod;

					calculatedFlexibility = true;

					unreleasedMethod.unreleasedMethod.unreleasedMethod("Building " + ((Integer) (busID + 1)).toString()
							+ " calculated its current active power flexibility");
				}

				// If the current flexibility is positive, set a load profile
				if (currentFlexibility > 0.0) {

					activePowerTarget = currentActivePowerTarget;

					UnreleasedClass targetLoadProfile = new UnreleasedClass();

					targetLoadProfile.unreleasedMethod(UnreleasedClass.unreleasedField,
							unreleasedMethod.unreleasedMethod, (int) Math.round(activePowerTarget));
					targetLoadProfile.unreleasedMethod(unreleasedMethod.unreleasedMethod + scheduleHorizon);

					unreleasedMethod.unreleasedMethod(targetLoadProfile);
					unreleasedMethod.unreleasedMethod.unreleasedMethod(
							"Building " + ((Integer) (busID + 1)).toString() + " is fulfilling a target load profile");

					unreleasedMethod.unreleasedMethod(activePowerTarget);
					activePowerTargetActive = true;
					unreleasedMethod.unreleasedMethod(activePowerTargetActive);
					calculatedFlexibility = false;
					unreleasedMethod.unreleasedMethod(calculatedFlexibility);
					currentFlexibility = 0.0;
				}
			}
		}

		// If apparent power curtailment can be and should be used
		if (useApparentPowerCurtailment && apparentPowerCurtailmentCriterion) {

			// If reactive power is used to regulate voltages and apparent power curtailment
			// is not active or higher than the standard target for transformer and line
			// criticality
			if (useQTargets && (apparentPowerCurtailmentTarget == -2.0 || apparentPowerCurtailmentTarget > 0.55)) {

				if (apparentPowerCurtailmentTarget == -2.0)

					unreleasedMethod.unreleasedMethod.unreleasedMethod("Building " + ((Integer) (busID + 1)).toString()
							+ " is starting to curtail its apparent power");

				if (apparentPowerCurtailmentSettingForTrafoTemp < 0.55)

					apparentPowerCurtailmentTarget = apparentPowerCurtailmentSettingForTrafoTemp;

				else

					apparentPowerCurtailmentTarget = 0.55;

				// If reactive power is not used to regulate voltages and apparent power
				// curtailment is not active or higher than the standard target for voltage
			} else if (!useQTargets
					&& (apparentPowerCurtailmentTarget == -2.0 || apparentPowerCurtailmentTarget > 0.45)) {

				if (apparentPowerCurtailmentTarget == -2.0)

					unreleasedMethod.unreleasedMethod.unreleasedMethod("Building " + ((Integer) (busID + 1)).toString()
							+ " is starting to curtail apparent apparent power");

				apparentPowerCurtailmentTarget = 0.45;
			}

			unreleasedMethod.unreleasedMethod(apparentPowerCurtailmentTarget);

			// Send command to UnreleasedClass to adjust active power of inverter
			UnreleasedClass UnreleasedClass = new UnreleasedClass(this.unreleasedMethod, pvControllerUUID,
					unreleasedMethod.unreleasedMethod, reactivePowerCompensationActive, reactivePowerTarget,
					reactivePowerCompensationDuration, apparentPowerCurtailmentTarget);

			unreleasedMethod.unreleasedMethod.unreleasedMethod(UnreleasedClass.class, UnreleasedClass);
		}

		// If apparent power curtailment is currently active but the apparent power
		// curtailment criterion was not fulfilled in the last X minutes
		if (apparentPowerCurtailmentTarget != -2.0 && apparentPowerCurtailmentCriterionLowEnough) {

			if (apparentPowerCurtailmentTarget >= 1.0) {

				unreleasedMethod.unreleasedMethod.unreleasedMethod("Building " + ((Integer) (busID + 1)).toString()
						+ " is stopping to curtail its apparent power");

				apparentPowerCurtailmentTarget = -2.0;

			} else

				apparentPowerCurtailmentTarget = Math.round((apparentPowerCurtailmentTarget + 0.01) * 100d) / 100d;

			unreleasedMethod.unreleasedMethod(apparentPowerCurtailmentTarget);

			// Send command to UnreleasedClass to adjust the active power of the inverter
			UnreleasedClass UnreleasedClass = new UnreleasedClass(this.unreleasedMethod, pvControllerUUID,
					unreleasedMethod.unreleasedMethod, reactivePowerCompensationActive, reactivePowerTarget,
					reactivePowerCompensationDuration, apparentPowerCurtailmentTarget);

			unreleasedMethod.unreleasedMethod.unreleasedMethod(UnreleasedClass.class, UnreleasedClass);
		}

		// If an active power target is currently used, but the active power flexibility
		// criterion was not fulfilled in the last X minutes, and reactive power base
		// voltage regulation and apparent power curtailment are not used, deactivate
		// the target load profile
		if (activePowerTargetActive && activePowerFlexibilityCriterionLowEnough && reactivePowerTarget == 0.0
				&& apparentPowerCurtailmentTarget == -2.0) {

			activePowerTargetActive = false;
			activePowerTarget = 1.0;

			unreleasedMethod.unreleasedMethod(null);
			unreleasedMethod.unreleasedMethod(activePowerTargetActive);
			unreleasedMethod.unreleasedMethod(activePowerTarget);
		}
	}

	// Method that implements distributed measures against mutually determined
	// critical situations when communication between buildings and to the DSO is
	// disturbed
	public void implementDistributedGridResilienceManagement() {

		long now = unreleasedMethod.unreleasedMethod;

		// Determine the current position of this building in the sorted available
		// active power flexibilities
		currentSortedBuildingFlexibilities = unreleasedMethod;

		// Determine current critical situations from the current calculated grid status
		// and the communicated transformer status
		UnreleasedClass bgsa = new UnreleasedClass(unreleasedMethod);
		// Set last maximum relevant line current, so that line current jumps can be
		// addressed
		bgsa.unreleasedMethod(lastILineMax);
		UnreleasedClass buildingCriticalSituations = bgsa.unreleasedMethod(latestCalculatedGridStatus,
				latestTransformerStatus);
		ArrayList<Double> iAbsValues = (ArrayList<Double>) latestCalculatedGridStatus.unreleasedMethod.unreleasedMethod;
		iAbsValues.remove(0);

		// Determine new maximum relevant line current, so that line current jumps can
		// be addressed
		lastILineMax = Collections.max(iAbsValues);

		// Initialize UnreleasedClass
		if (hpic == null) {

			hpic = new UnreleasedClass(unreleasedMethod);
		}

		List<String[]> influenceablePerimeterCriticality;

		// Determine which critical situations this building can influence from its
		// position in the grid
		try {

			influenceablePerimeterCriticality = hpic.unreleasedMethod(buildingCriticalSituations,
					latestCalculatedGridStatus, (unreleasedMethod.unreleasedMethod == null
							|| unreleasedMethod.unreleasedMethod.unreleasedMethod <= now));

		} catch (UnreleasedClass e) {

			e.printStackTrace();
			return;
		}

		double uMostCriticalPerimeter = 1.0;
		boolean lineOrTrafoCritical = false;

		// If there was a line current jump and the communication to the DSO may be
		// disturbed, set the line criticality to zero
		if (lineCriticality > 2 && firstTimeLineWasCritical + 1 * 60 < now || lineCriticality <= 2
				|| !communicationToDSOActive) {

			// If there was a line current jump and the communication to the DSO is still
			// assumed to be online, assume communication to the DSO to be offline
			if (communicationToDSOActive && lineCriticality > 2
					&& firstTimeLineWasCritical > lastTimeDSOMessageReceived + 60
					&& (useApparentPowerCurtailment
							&& (apparentPowerCurtailmentTarget > apparentPowerCurtailmentSettingForCurrent
									|| apparentPowerCurtailmentTarget == -2.0
									|| apparentPowerCurtailmentSettingForCurrent == 1.0))) {

				communicationToDSOActive = false;

				unreleasedMethod.unreleasedMethod.unreleasedMethod("Building " + ((Integer) (busID + 1)).toString()
						+ ": Communication to DSO is probably disturbed.");
			}

			lineCriticality = 0;
		}

		trafoCriticality = 0;

		// Determine the values of uMostCriticalPerimeterID, trafoCriticality and
		// lineCriticality from the influenceable critical situations
		for (int i = 0; i < influenceablePerimeterCriticality.size(); i++) {

			if (influenceablePerimeterCriticality.get(i)[busID].split(";")[0].equals("Bus critical")) {

				uMostCriticalPerimeterID = Integer
						.valueOf(influenceablePerimeterCriticality.get(i)[busID].split(";")[1]);

			} else if (influenceablePerimeterCriticality.get(i)[busID].split(";")[0].equals("Line critical")) {

				if (influenceablePerimeterCriticality.get(i)[busID].split(";")[1].equals("0")) {

					trafoCriticality = Integer.valueOf(influenceablePerimeterCriticality.get(i)[busID].split(";")[2]);

				} else {

					if (lineCriticality < 2
							|| Integer.valueOf(influenceablePerimeterCriticality.get(i)[busID].split(";")[2]) == 2) {

						lineCriticality = Integer
								.valueOf(influenceablePerimeterCriticality.get(i)[busID].split(";")[2]);
					}
				}
			}
		}

		// Save the new line criticality and remove the oldest one from a list that
		// holds the line criticalities of the last X minutes
		if (lastXMinutesOfMaxLineCurrents.isEmpty()) {

			for (int i = (int) (numberOfMinutesCriticalCurrent * 60 / gridSimulationUpdateInterval - 1); i >= 0; i--) {

				lastXMinutesOfMaxLineCurrents.add(lineCriticality == 2 || lineCriticality == 1);
			}
		} else {

			lastXMinutesOfMaxLineCurrents.add(lineCriticality == 2 || lineCriticality == 1);
			lastXMinutesOfMaxLineCurrents.remove(0);
		}

		// If no relevant line was critical in the last X minutes, the line current is
		// uncritical long enough so that measures can be scaled back
		boolean lineCurrentUncriticalLongEnough = !lastXMinutesOfMaxLineCurrents.contains(true);

		// Combine line and transformer criticality into a single variable
		if (trafoCriticality != 0 || lineCriticality != 0 && lineCriticality <= 2)

			lineOrTrafoCritical = true;

		// Save the new combined line and transformer criticality and remove the oldest
		// one from a list that holds the line criticalities of the last X minutes
		if (lastXMinutesOfLineOrTrafoCriticality.isEmpty()) {

			for (int i = (int) (numberOfMinutesLineOrTrafoCriticality * 60 / gridSimulationUpdateInterval
					- 1); i >= 0; i--) {

				lastXMinutesOfLineOrTrafoCriticality.add(lineOrTrafoCritical);
			}
		} else {

			lastXMinutesOfLineOrTrafoCriticality.add(lineOrTrafoCritical);
			lastXMinutesOfLineOrTrafoCriticality.remove(0);
		}
		// If no relevant line or the transformer was critical in the last X minutes,
		// the line and transformer criticality is low enough so that measures can be
		// scaled back
		boolean lineAndTrafoUncriticalLongEnough = !lastXMinutesOfLineOrTrafoCriticality.contains(true);

		// Get the voltage of the most critical node within a perimeter of the building
		uMostCriticalPerimeter = latestCalculatedGridStatus.unreleasedMethod.get(uMostCriticalPerimeterID);

		// Save the new most critical voltage in the building's perimeter and remove the
		// oldest one from a list that holds the of voltages of the last ten minutes
		if (lastTenMinutesOfMostCriticalPerimeterVoltages.isEmpty()) {

			for (int i = (int) (10 * 60 / gridSimulationUpdateInterval - 1); i >= 0; i--) {

				lastTenMinutesOfMostCriticalPerimeterVoltages.add(uMostCriticalPerimeter);
			}
		} else {

			lastTenMinutesOfMostCriticalPerimeterVoltages.add(uMostCriticalPerimeter);
			lastTenMinutesOfMostCriticalPerimeterVoltages.remove(0);
		}

		// Save the new most critical voltage in the building's perimeter and remove the
		// oldest one from a list that holds the of voltages of the last X minutes. This
		// differs from lastTenMinutesOfMostCriticalPerimeterVoltages in that voltage
		// jumps are considered as well
		if (lastXMinutesOfCriticalVoltagesForCurtailment.isEmpty()) {

			for (int i = (int) (numberOfMinutesCriticalVoltage * 60 / gridSimulationUpdateInterval - 1); i >= 0; i--) {

				lastXMinutesOfCriticalVoltagesForCurtailment
						.add(uMostCriticalPerimeter > 1.08 || lastTenMinutesOfMostCriticalPerimeterVoltages
								.get(lastTenMinutesOfMostCriticalPerimeterVoltages.size() - 1)
								- lastTenMinutesOfMostCriticalPerimeterVoltages
										.get(lastTenMinutesOfMostCriticalPerimeterVoltages.size() - 2) > 0.01
								&& lastTenMinutesOfMostCriticalPerimeterVoltages
										.get(lastTenMinutesOfMostCriticalPerimeterVoltages.size() - 2) > 1.05);
			}
		} else {

			lastXMinutesOfCriticalVoltagesForCurtailment
					.add(uMostCriticalPerimeter > 1.08 || lastTenMinutesOfMostCriticalPerimeterVoltages
							.get(lastTenMinutesOfMostCriticalPerimeterVoltages.size() - 1)
							- lastTenMinutesOfMostCriticalPerimeterVoltages
									.get(lastTenMinutesOfMostCriticalPerimeterVoltages.size() - 2) > 0.01
							&& lastTenMinutesOfMostCriticalPerimeterVoltages
									.get(lastTenMinutesOfMostCriticalPerimeterVoltages.size() - 2) > 1.05);

			lastXMinutesOfCriticalVoltagesForCurtailment.remove(0);
		}

		// If no relevant node voltages was critical in the last X minutes, the voltage
		// is uncritical long enough so that measures can be scaled back
		boolean voltageUncriticalLongEnough = !lastXMinutesOfCriticalVoltagesForCurtailment.contains(true);

		double newReactivePowerTarget = reactivePowerTarget;

		boolean uMostCriticalPerimeterLowEnough = true;
		boolean uMostCriticalPerimeterHighEnough = true;

		// Determine if the perimeter voltages have been low enough or high enough to
		// reduce reactive power feed-in or draw
		for (int i = (int) (10 * 60 / gridSimulationUpdateInterval - 1); i >= 0; i--) {

			if (lastTenMinutesOfMostCriticalPerimeterVoltages.get(i) != null
					&& lastTenMinutesOfMostCriticalPerimeterVoltages.get(i) >= 1.08) {

				uMostCriticalPerimeterLowEnough = false;
			}

			if (lastTenMinutesOfMostCriticalPerimeterVoltages.get(i) != null
					&& lastTenMinutesOfMostCriticalPerimeterVoltages.get(i) <= 0.925) {

				uMostCriticalPerimeterHighEnough = false;
			}
		}

		// If no or positive reactive power is already used
		if (reactivePowerTarget >= 0.0) {

			// If the local voltage is very high or if the current reactive power target is
			// zero and there was a voltage jump at the most critical node
			if (buildingClearance && (latestCalculatedGridStatus.unreleasedMethod.get(busID) > 1.09
					|| lastTenMinutesOfMostCriticalPerimeterVoltages
							.get(lastTenMinutesOfMostCriticalPerimeterVoltages.size() - 1)
							- lastTenMinutesOfMostCriticalPerimeterVoltages
									.get(lastTenMinutesOfMostCriticalPerimeterVoltages.size() - 2) > 0.01
							&& lastTenMinutesOfMostCriticalPerimeterVoltages
									.get(lastTenMinutesOfMostCriticalPerimeterVoltages.size() - 2) > 1.05
							&& reactivePowerTarget == 0.0))

				newReactivePowerTarget = reactivePowerTarget + 0.5;

			// If the local voltage is high and the current reactive power target is zero or
			// if the current reactive power target is below 0.5 and there was a voltage
			// jump at the most critical node
			else if (buildingClearance
					&& (latestCalculatedGridStatus.unreleasedMethod.get(busID) > 1.085 && reactivePowerTarget == 0.0
							|| lastTenMinutesOfMostCriticalPerimeterVoltages
									.get(lastTenMinutesOfMostCriticalPerimeterVoltages.size() - 1)
									- lastTenMinutesOfMostCriticalPerimeterVoltages
											.get(lastTenMinutesOfMostCriticalPerimeterVoltages.size() - 2) > 0.01
									&& lastTenMinutesOfMostCriticalPerimeterVoltages
											.get(lastTenMinutesOfMostCriticalPerimeterVoltages.size() - 2) > 1.05
									&& reactivePowerTarget <= 0.5))

				newReactivePowerTarget = reactivePowerTarget + 0.25;

			// If the most critical perimeter voltage is high
			else if (buildingClearance && uMostCriticalPerimeter > 1.085)

				newReactivePowerTarget = reactivePowerTarget + 0.01;

			// If the most critical perimeter voltage was low enough for the last ten
			// minutes, but the reactive power target is positive
			else if ((!buildingClearance || uMostCriticalPerimeterLowEnough
					|| latestCalculatedGridStatus.unreleasedMethod.get(busID) < 1.0) && reactivePowerTarget > 0.0) {

				newReactivePowerTarget = reactivePowerTarget - 0.01;

				// Correction if the reactive power target got smaller than 0.0
				if (newReactivePowerTarget < 0.0)

					newReactivePowerTarget = 0.0;
			}
			// Correction if the reactive power target got larger than 1.0
			if (newReactivePowerTarget > 1.0)

				newReactivePowerTarget = 1.0;
		}

		// If no or negative reactive power is already used
		if (reactivePowerTarget <= 0.0) {

			// If the local voltage is very low
			if (buildingClearance && latestCalculatedGridStatus.unreleasedMethod.get(busID) < 0.91)

				newReactivePowerTarget = reactivePowerTarget - 0.5;

			// If the local voltage is low and the current reactive power target is zero
			else if (buildingClearance && latestCalculatedGridStatus.unreleasedMethod.get(busID) < 0.92
					&& reactivePowerTarget == 0.0)

				newReactivePowerTarget = reactivePowerTarget - 0.25;

			// If the most critical perimeter voltage is low
			else if (buildingClearance && uMostCriticalPerimeter < 0.92)

				newReactivePowerTarget = reactivePowerTarget - 0.01;

			// If the most critical perimeter voltage was high enough for the last ten
			// minutes, but the reactive power target is negative
			else if ((!buildingClearance || uMostCriticalPerimeterHighEnough
					|| latestCalculatedGridStatus.unreleasedMethod.get(busID) > 1.0) && reactivePowerTarget < 0.0) {

				newReactivePowerTarget = reactivePowerTarget + 0.01;

				// Correction if the reactive power target got larger than 0.0
				if (newReactivePowerTarget > 0.0)

					newReactivePowerTarget = 0.0;
			}
			// Correction if the reactive power target got smaller than -1.0
			if (newReactivePowerTarget < -1.0)

				newReactivePowerTarget = -1.0;
		}

		// Correction to prevent reactive power target steps smaller than 0.01
		newReactivePowerTarget = Math.round(newReactivePowerTarget * 100d) / 100d;

		// THE FOLLOWING VARIABLES SERVE THE PURPOSE OF DETECTING DSO COMMUNICATION
		// DISTURBANCES

		if (firstTimeVoltageWasCritical == Long.MAX_VALUE - 10000000 && newReactivePowerTarget != reactivePowerTarget) {

			firstTimeVoltageWasCritical = now;
			firstTimeVoltageWasUncritical = Long.MAX_VALUE - 10000000;
		}

		if (newReactivePowerTarget == reactivePowerTarget) {

			firstTimeVoltageWasCritical = Long.MAX_VALUE - 10000000;
		}

		if (firstTimeVoltageWasUncritical == Long.MAX_VALUE - 10000000 && (voltageUncriticalLongEnough
				|| apparentPowerCurtailmentTarget < apparentPowerCurtailmentSettingForVoltage
						&& apparentPowerCurtailmentTarget != -2.0)) {

			firstTimeVoltageWasUncritical = now;
		}

		if (firstTimeLineWasCritical == Long.MAX_VALUE - 10000000 && lineCriticality != 0) {

			firstTimeLineWasCritical = now;
			firstTimeLineWasUncritical = Long.MAX_VALUE - 10000000;
		}

		if (firstTimeLineWasCritical != Long.MAX_VALUE - 10000000 && lineCriticality == 0) {

			firstTimeLineWasCritical = Long.MAX_VALUE - 10000000;
		}

		if (firstTimeLineWasUncritical == Long.MAX_VALUE - 10000000 && (lineCurrentUncriticalLongEnough
				|| apparentPowerCurtailmentTarget < apparentPowerCurtailmentSettingForCurrent
						&& apparentPowerCurtailmentTarget != -2.0)) {

			firstTimeLineWasUncritical = now;
		}

		if (firstTimeLineWasHighlyCritical == Long.MAX_VALUE - 10000000 && lineCriticality == 2) {

			firstTimeLineWasHighlyCritical = now;
		}

		if (firstTimeLineWasHighlyCritical != Long.MAX_VALUE - 10000000 && lineCriticality != 2) {

			firstTimeLineWasHighlyCritical = Long.MAX_VALUE - 10000000;
		}

		if (firstTimeLineOrTrafoWasCritical == Long.MAX_VALUE - 10000000 && lineOrTrafoCritical)

			firstTimeLineOrTrafoWasCritical = now;

		if (firstTimeLineOrTrafoWasCritical != Long.MAX_VALUE - 10000000 && !lineOrTrafoCritical)

			firstTimeLineOrTrafoWasCritical = Long.MAX_VALUE - 10000000;

		if ((firstTimeTrafoWasHighlyCritical == Long.MAX_VALUE - 10000000
				|| lastTransformerTemp > latestTransformerStatus.unreleasedMethod.unreleasedMethod)
				&& trafoCriticality == 3) {

			firstTimeTrafoWasHighlyCritical = now;
			firstTimeTrafoWasNotHighlyCritical = Long.MAX_VALUE - 10000000;
		}

		if (firstTimeTrafoWasHighlyCritical != Long.MAX_VALUE - 10000000 && trafoCriticality < 3) {

			firstTimeTrafoWasHighlyCritical = Long.MAX_VALUE - 10000000;
			firstTimeTrafoWasNotHighlyCritical = now;
		}

		// Criterion for the implementation of reactive power compensation
		criticalityCriterionForReactiveCompensation = (lineOrTrafoCritical || apparentPowerCurtailmentTarget != -2.0)
				&& (firstTimeLineOrTrafoWasCritical + 1 * 60 < now || !communicationToDSOActive);

		// Criteria for the implementation of active power targets
		voltageCriticalityCriterionForActiveTargets = buildingClearance
				&& (reactivePowerTarget == 1.0 || reactivePowerTarget == -1.0
						|| !useQTargets && newReactivePowerTarget != 0.0 || apparentPowerCurtailmentTarget != -2.0)
				&& (firstTimeVoltageWasCritical + 16 * 60 < now || !communicationToDSOActive);

		lineAndTrafoCriticalityCriterionForActiveTargets = (lineOrTrafoCritical
				|| apparentPowerCurtailmentTarget != -2.0)
				&& (firstTimeLineOrTrafoWasCritical + 16 * 60 < now || !communicationToDSOActive);

		// Criteria for the implementation of apparent power curtailment
		voltageCriticalityCriterionForCurtailment = buildingClearance && !useQTargets
				&& (uMostCriticalPerimeter > 1.085 || lastTenMinutesOfMostCriticalPerimeterVoltages
						.get(lastTenMinutesOfMostCriticalPerimeterVoltages.size() - 1)
						- lastTenMinutesOfMostCriticalPerimeterVoltages
								.get(lastTenMinutesOfMostCriticalPerimeterVoltages.size() - 2) > 0.01
						&& lastTenMinutesOfMostCriticalPerimeterVoltages
								.get(lastTenMinutesOfMostCriticalPerimeterVoltages.size() - 2) > 1.05
						&& !buildingStateExchangeMap.isEmpty())
				&& (apparentPowerCurtailmentTarget > 0.4 || apparentPowerCurtailmentTarget == -2.0)
				&& (firstTimeVoltageWasCritical + 1 * 60 < now || !communicationToDSOActive);

		lineCriticalityCriterionForCurtailment = lineCriticality != 0 && lineCriticality != 2
				&& (firstTimeLineWasCritical + 1 * 60 < now || !communicationToDSOActive)
				|| lineCriticality == 2 && (firstTimeLineWasHighlyCritical + 1 * 60 < now || !communicationToDSOActive);

		trafoCriticalityCriterionForCurtailment = trafoCriticality == 3
				&& (firstTimeTrafoWasHighlyCritical + 1 * 60 < now || !communicationToDSOActive);

		// Criteria for the lowering of apparent power curtailment
		voltageCriticalityCriterionForCurtailmentLowering = useQTargets || (voltageUncriticalLongEnough
				|| apparentPowerCurtailmentTarget < apparentPowerCurtailmentSettingForVoltage
						&& apparentPowerCurtailmentTarget != -2.0)
				&& (firstTimeVoltageWasUncritical + 1 * 60 < now || !communicationToDSOActive);

		lineCriticalityCriterionForCurtailmentLowering = (lineCurrentUncriticalLongEnough
				|| apparentPowerCurtailmentTarget < apparentPowerCurtailmentSettingForCurrent
						&& apparentPowerCurtailmentTarget != -2.0)
				&& (firstTimeLineWasUncritical + 1 * 60 < now || !communicationToDSOActive);

		trafoCriticalityCriterionForCurtailmentLowering = trafoCriticality < 3
				&& (firstTimeTrafoWasNotHighlyCritical + 1 * 60 < now || !communicationToDSOActive);

		// Check if the critical situation requires a reactive power target
		if (useQTargets && newReactivePowerTarget != reactivePowerTarget
				&& (!communicationToDSOActive || firstTimeVoltageWasCritical + 1 * 60 < now)) {

			// Since a reactive target is required, but the DSO did not send one, assume the
			// communication to the DSO to be offline
			if (communicationToDSOActive) {

				communicationToDSOActive = false;

				unreleasedMethod.unreleasedMethod.unreleasedMethod("Building " + ((Integer) (busID + 1)).toString()
						+ ": Communication to DSO is probably disturbed.");
			}

			if (reactivePowerTarget == 0.0)

				unreleasedMethod.unreleasedMethod.unreleasedMethod(
						"Building " + ((Integer) (busID + 1)).toString() + " is implementing a reactive power target");

			else if (newReactivePowerTarget == 0.0)

				unreleasedMethod.unreleasedMethod.unreleasedMethod("Building " + ((Integer) (busID + 1)).toString()
						+ " is not implementing a reactive power target anymore");

			reactivePowerTarget = newReactivePowerTarget;
			unreleasedMethod.unreleasedMethod(reactivePowerTarget);
			reactivePowerCompensationActive = false;
			unreleasedMethod.unreleasedMethod(false);

			// Send command to UnreleasedClass to adjust reactive power of inverter
			UnreleasedClass UnreleasedClass = new UnreleasedClass(this.unreleasedMethod, pvControllerUUID,
					unreleasedMethod.unreleasedMethod, reactivePowerCompensationActive, newReactivePowerTarget,
					reactivePowerCompensationDuration, apparentPowerCurtailmentTarget);

			unreleasedMethod.unreleasedMethod.unreleasedMethod(UnreleasedClass.class, UnreleasedClass);
		}

		// Check if the critical situation requires a reactive power compensation
		if (useQTargets && newReactivePowerTarget == 0.0 && reactivePowerTarget == 0.0
				&& !reactivePowerCompensationActive && criticalityCriterionForReactiveCompensation) {

			// Since reactive power compensation is required, but the DSO did not send a
			// corresponding command, assume the communication to the DSO to be offline
			if (communicationToDSOActive) {

				communicationToDSOActive = false;

				unreleasedMethod.unreleasedMethod.unreleasedMethod("Building " + ((Integer) (busID + 1)).toString()
						+ ": Communication to DSO is probably disturbed.");
			}

			reactivePowerCompensationActive = true;
			unreleasedMethod.unreleasedMethod(true);
			lastTimeReactivePowerCompensationActivated = now;

			// Send command to UnreleasedClass to adjust reactive power of inverter
			UnreleasedClass UnreleasedClass = new UnreleasedClass(this.unreleasedMethod, pvControllerUUID,
					unreleasedMethod.unreleasedMethod, reactivePowerCompensationActive, newReactivePowerTarget,
					reactivePowerCompensationDuration, apparentPowerCurtailmentTarget);

			unreleasedMethod.unreleasedMethod.unreleasedMethod(UnreleasedClass.class, UnreleasedClass);

			unreleasedMethod.unreleasedMethod.unreleasedMethod(
					"Building " + ((Integer) (busID + 1)).toString() + " is compensating its reactive power");

			// Check if the critical situation requires an active power flexibility
		} else if (usePTargets && !activePowerTargetActive
				&& (voltageCriticalityCriterionForActiveTargets || lineAndTrafoCriticalityCriterionForActiveTargets)) {

			// Since active power flexibility is required, but the DSO did not send a
			// corresponding command, assume the communication to the DSO to be offline
			if (communicationToDSOActive) {

				communicationToDSOActive = false;

				unreleasedMethod.unreleasedMethod.unreleasedMethod("Building " + ((Integer) (busID + 1)).toString()
						+ ": Communication to DSO is probably disturbed.");
			}

			int currentActivePowerTarget = 0;

			UnreleasedClass targetLoadProfile = new UnreleasedClass();

			targetLoadProfile.unreleasedMethod(UnreleasedClass.unreleasedField, now,
					(int) Math.round(currentActivePowerTarget));
			targetLoadProfile.unreleasedMethod(now + scheduleHorizon);

			// If the current active power flexibility has not been calculated, it is
			// calculated
			if (!activePowerTargetActive && (!calculatedFlexibility
					|| lastTimeBuildingFlexibilityCalculated + 900 < unreleasedMethod.unreleasedMethod)) {

				UnreleasedClass fd = new UnreleasedClass(unreleasedMethod.unreleasedMethod.unreleasedMethod);

				fd.unreleasedMethod(scheduleHorizon / 60);
				fd.unreleasedMethod(scheduleHorizon);
				fd.unreleasedMethod(resolution);
				fd.unreleasedMethod(now);
				fd.unreleasedMethod(currentActivePowerTarget);

				unreleasedMethod.unreleasedMethod(fd);

				// Tell UnreleasedClass to calculate the current active power
				// flexibility
				calculateFlexibility();

				currentFlexibility = unreleasedMethod.unreleasedMethod;

				calculatedFlexibility = true;

				unreleasedMethod.unreleasedMethod.unreleasedMethod("Building " + ((Integer) (busID + 1)).toString()
						+ " calculated its current active power flexibility");
			}

			boolean buildingShouldReact = false;

			// Determine if the building is among the group of the most flexible buildings
			for (int i = 0; i < currentSortedBuildingFlexibilities.size(); i++) {

				buildingShouldReact = buildingShouldReact || currentSortedBuildingFlexibilities.get(i) == busID;

				if (i == 4)
					break;
			}

			// Set load profile
			if (buildingShouldReact) {

				unreleasedMethod.unreleasedMethod(targetLoadProfile);
				unreleasedMethod.unreleasedMethod.unreleasedMethod(
						"Building " + ((Integer) (busID + 1)).toString() + " is fulfilling a target load profile");

				activePowerTarget = currentActivePowerTarget;
				unreleasedMethod.unreleasedMethod(activePowerTarget);
				activePowerTargetActive = true;
				unreleasedMethod.unreleasedMethod(activePowerTargetActive);
				calculatedFlexibility = false;
				unreleasedMethod.unreleasedMethod(calculatedFlexibility);
				currentFlexibility = 0.0;
			}
		}

		// Check if a critical situation requires apparent power curtailment
		if (useApparentPowerCurtailment
				&& latestCalculatedGridStatus.unreleasedMethod.stream().mapToDouble(Double::doubleValue).sum() > 0
				&& (voltageCriticalityCriterionForCurtailment || lineCriticalityCriterionForCurtailment
						|| trafoCriticalityCriterionForCurtailment)) {

			if (apparentPowerCurtailmentTarget == -2.0)

				unreleasedMethod.unreleasedMethod.unreleasedMethod("Building " + ((Integer) (busID + 1)).toString()
						+ " is starting to curtail its apparent power");

			// Check if apparent power curtailment is needed due to critical voltages
			if (voltageCriticalityCriterionForCurtailment) {

				// Since apparent power curtailment is required, but the DSO did not send a
				// corresponding command, assume the communication to the DSO to be offline
				if (communicationToDSOActive) {

					communicationToDSOActive = false;

					unreleasedMethod.unreleasedMethod.unreleasedMethod("Building " + ((Integer) (busID + 1)).toString()
							+ ": Communication to DSO is probably disturbed.");
				}

				// If the voltage is too high, reduce the apparent power curtailment target
				// according to the following algorithm
				if (uMostCriticalPerimeter > 1.085) {

					if (apparentPowerCurtailmentTarget == -2.0 || apparentPowerCurtailmentTarget > 0.8
							|| apparentPowerCurtailmentTarget > apparentPowerCurtailmentSettingForVoltage) {

						if (apparentPowerCurtailmentSettingForVoltage < 0.8)

							apparentPowerCurtailmentTarget = apparentPowerCurtailmentSettingForVoltage;

						else

							apparentPowerCurtailmentTarget = 0.8;

					} else if (apparentPowerCurtailmentTarget > 0.7)

						apparentPowerCurtailmentTarget = 0.7;

					else if (apparentPowerCurtailmentTarget > 0.65)

						apparentPowerCurtailmentTarget = 0.65;

					else
						apparentPowerCurtailmentTarget = Math.round((apparentPowerCurtailmentTarget - 0.01) * 100d)
								/ 100d;

					if (apparentPowerCurtailmentTarget < apparentPowerCurtailmentSettingForVoltage) {

						apparentPowerCurtailmentSettingForVoltage = apparentPowerCurtailmentTarget;
						unreleasedMethod.unreleasedMethod(apparentPowerCurtailmentTarget);
					}

				} else if (apparentPowerCurtailmentTarget > apparentPowerCurtailmentSettingForVoltage) {

					apparentPowerCurtailmentTarget = apparentPowerCurtailmentSettingForVoltage;

				} else

					apparentPowerCurtailmentTarget = 0.4;

				unreleasedMethod.unreleasedMethod.unreleasedMethod("Building " + ((Integer) (busID + 1)).toString()
						+ " is curtailing its apparent power due to voltage");
			}
			// Check if preventive apparent power curtailment is needed due to slighty high
			// line current or line current jumps
			else if ((lineCriticality == 1 && apparentPowerCurtailmentSettingForCurrent < 1.0 || lineCriticality > 2)
					&& (apparentPowerCurtailmentTarget > apparentPowerCurtailmentSettingForCurrent
							|| apparentPowerCurtailmentTarget == -2.0
							|| apparentPowerCurtailmentSettingForCurrent == 1.0)) {

				// Since apparent power curtailment is required, but the DSO did not send a
				// corresponding command, assume the communication to the DSO to be offline
				if (communicationToDSOActive) {

					communicationToDSOActive = false;

					unreleasedMethod.unreleasedMethod.unreleasedMethod("Building " + ((Integer) (busID + 1)).toString()
							+ ": Communication to DSO is probably disturbed.");
				}

				if (apparentPowerCurtailmentSettingForCurrent < 1.0)

					apparentPowerCurtailmentTarget = apparentPowerCurtailmentSettingForCurrent;

				else if (Collections.min(apparentPowerCurtailmentSettingsForCurrent.values().stream()
						.filter(s -> s >= 0.0).collect(Collectors.toList())) < 1.0)

					apparentPowerCurtailmentTarget = Collections.min(apparentPowerCurtailmentSettingsForCurrent.values()
							.stream().filter(s -> s >= 0.0).collect(Collectors.toList()));

				else

					apparentPowerCurtailmentTarget = 0.4;

				unreleasedMethod.unreleasedMethod.unreleasedMethod("Building " + ((Integer) (busID + 1)).toString()
						+ " is curtailing its apparent power due to line current jump or low line criticality");
			}
			// Check if apparent power curtailment is needed due to high line
			// current or high transformer temperature. Only use reactive power due to
			// transformer temperature if the apparent power curtailment target is higher
			// than or equal to those of other buildings
			else if (lineCriticality == 2
					&& (apparentPowerCurtailmentTarget > 0.4 || apparentPowerCurtailmentTarget == -2.0)
					|| trafoCriticality == 3
							&& !(lastTransformerTemp > latestTransformerStatus.unreleasedMethod.unreleasedMethod)
							&& (apparentPowerCurtailmentTarget == Collections
									.max(apparentPowerCurtailmentTargets.values())
									&& Collections.min(apparentPowerCurtailmentTargets.values()) != -2.0
									|| apparentPowerCurtailmentTarget == -2.0)
					|| apparentPowerCurtailmentTarget > apparentPowerCurtailmentSettingForTrafoTemp) {

				// Since apparent power curtailment is required, but the DSO did not send a
				// corresponding command, assume the communication to the DSO to be offline
				if (communicationToDSOActive) {

					communicationToDSOActive = false;

					unreleasedMethod.unreleasedMethod.unreleasedMethod("Building " + ((Integer) (busID + 1)).toString()
							+ ": Communication to DSO is probably disturbed.");
				}

				// Check if apparent power curtailment is needed due to high line current and
				// determine the apparent power curtailment target according to the following
				// algorithm
				if (lineCriticality == 2) {

					if (apparentPowerCurtailmentTarget == -2.0 || apparentPowerCurtailmentTarget > 0.8
							|| apparentPowerCurtailmentTarget > apparentPowerCurtailmentSettingForCurrent) {

						if (apparentPowerCurtailmentSettingForCurrent < 0.8)

							apparentPowerCurtailmentTarget = apparentPowerCurtailmentSettingForCurrent;

						else

							apparentPowerCurtailmentTarget = 0.8;

					} else if (apparentPowerCurtailmentTarget > 0.7)

						apparentPowerCurtailmentTarget = 0.7;

					else if (apparentPowerCurtailmentTarget > 0.65)

						apparentPowerCurtailmentTarget = 0.65;

					else

						apparentPowerCurtailmentTarget = Math.round((apparentPowerCurtailmentTarget - 0.01) * 100d)
								/ 100d;

					if (apparentPowerCurtailmentTarget < apparentPowerCurtailmentSettingForCurrent
							&& (apparentPowerCurtailmentTarget >= 0.65
									|| Math.round(apparentPowerCurtailmentTarget * 100d) / 100d == Math
											.round((apparentPowerCurtailmentSettingForCurrent - 0.01) * 100d) / 100d)) {

						apparentPowerCurtailmentSettingForCurrent = apparentPowerCurtailmentTarget;
						unreleasedMethod.unreleasedMethod(apparentPowerCurtailmentTarget);
					}
				}
				// Check if apparent power curtailment is needed due to high transformer
				// temperature and determine the apparent power curtailment target according to
				// the following algorithm
				else {

					if (apparentPowerCurtailmentTarget == -2.0 && apparentPowerCurtailmentSettingForTrafoTemp < 0.9
							|| apparentPowerCurtailmentTarget > apparentPowerCurtailmentSettingForTrafoTemp
									&& apparentPowerCurtailmentTarget > 0.6) {

						if (apparentPowerCurtailmentSettingForTrafoTemp >= 0.6)

							apparentPowerCurtailmentTarget = apparentPowerCurtailmentSettingForTrafoTemp;

						else

							apparentPowerCurtailmentTarget = 0.6;

					} else if (apparentPowerCurtailmentTarget > 0.9 || apparentPowerCurtailmentTarget == -2.0)

						apparentPowerCurtailmentTarget = 0.9;

					else if (apparentPowerCurtailmentTarget > 0.8)

						apparentPowerCurtailmentTarget = 0.8;

					else if (apparentPowerCurtailmentTarget > 0.7)

						apparentPowerCurtailmentTarget = 0.7;

					else if (apparentPowerCurtailmentTarget > 0.65)

						apparentPowerCurtailmentTarget = 0.65;

					else

						apparentPowerCurtailmentTarget = Math.round((apparentPowerCurtailmentTarget - 0.01) * 100d)
								/ 100d;

					if (apparentPowerCurtailmentTarget < apparentPowerCurtailmentSettingForTrafoTemp
							&& (apparentPowerCurtailmentTarget >= 0.65
									|| Math.round(apparentPowerCurtailmentTarget * 100d) / 100d == Math.round(
											(apparentPowerCurtailmentSettingForTrafoTemp - 0.01) * 100d) / 100d)) {

						apparentPowerCurtailmentSettingForTrafoTemp = apparentPowerCurtailmentTarget;
						unreleasedMethod.unreleasedMethod(apparentPowerCurtailmentTarget);
					}
				}
			}

			unreleasedMethod.unreleasedMethod(apparentPowerCurtailmentTarget);

			UnreleasedClass UnreleasedClass = new UnreleasedClass(this.unreleasedMethod, pvControllerUUID,
					unreleasedMethod.unreleasedMethod, reactivePowerCompensationActive, reactivePowerTarget,
					reactivePowerCompensationDuration, apparentPowerCurtailmentTarget);

			unreleasedMethod.unreleasedMethod.unreleasedMethod(UnreleasedClass.class, UnreleasedClass);
		}

		// Check if the apparent power curtailment target can be scaled back (increased)
		if (apparentPowerCurtailmentTarget != -2.0 && voltageCriticalityCriterionForCurtailmentLowering
				&& lineCriticalityCriterionForCurtailmentLowering && trafoCriticalityCriterionForCurtailmentLowering) {

			// Since the current extend of apparent power curtailment is not required, but
			// the DSO did not send a corresponding command, assume the communication to the
			// DSO to be offline
			if (communicationToDSOActive) {

				communicationToDSOActive = false;

				unreleasedMethod.unreleasedMethod.unreleasedMethod("Building " + ((Integer) (busID + 1)).toString()
						+ ": Communication to DSO is probably disturbed.");
			}

			if (apparentPowerCurtailmentTarget >= 1.0) {

				apparentPowerCurtailmentTarget = -2.0;

				unreleasedMethod.unreleasedMethod.unreleasedMethod("Building " + ((Integer) (busID + 1)).toString()
						+ " is stopping to curtail its apparent power");
			} else

				apparentPowerCurtailmentTarget = Math.round((apparentPowerCurtailmentTarget + 0.01) * 100d) / 100d;

			unreleasedMethod.unreleasedMethod(apparentPowerCurtailmentTarget);

			UnreleasedClass pvExchange = new UnreleasedClass(this.unreleasedMethod, pvControllerUUID,
					unreleasedMethod.unreleasedMethod, reactivePowerCompensationActive, reactivePowerTarget,
					reactivePowerCompensationDuration, apparentPowerCurtailmentTarget);

			unreleasedMethod.unreleasedMethod.unreleasedMethod(UnreleasedClass.class, pvExchange);
		}

		lastTransformerTemp = latestTransformerStatus.unreleasedMethod.unreleasedMethod;

		// If an active power target is currently used, but the active power flexibility
		// is not needed anymore, and reactive power based voltage regulation and
		// apparent power curtailment are not used, deactivate the target load profile
		if (!communicationToDSOActive && activePowerTargetActive && lineAndTrafoUncriticalLongEnough
				&& reactivePowerTarget == 0.0 && apparentPowerCurtailmentTarget == -2.0) {

			activePowerTargetActive = false;
			activePowerTarget = 1.0;

			unreleasedMethod.unreleasedMethod(null);
			unreleasedMethod.unreleasedMethod(activePowerTargetActive);
			unreleasedMethod.unreleasedMethod(activePowerTarget);
		}
	}

	// Calculate the current ability to improve a critical situation with active
	// power flexibility
	public void calculateFlexibility() {

		unreleasedMethod.notify(UnreleasedClass.unreleasedField);

		UnreleasedClass fd = unreleasedMethod.unreleasedMethod;

		currentFlexibility = fd.unreleasedMethod;

		lastTimeBuildingFlexibilityCalculated = unreleasedMethod.unreleasedMethod;
		calculatedFlexibility = true;

	}

	public double getLocalBusVoltage(UnreleasedClass gsdo) {

		String uuid = unreleasedMethod.unreleasedMethod.unreleasedMethod.toString();
		uuid = uuid.substring(uuid.length() - 4);
		Integer buildingNumber = Integer.parseInt(uuid) - 1; // Bus number of this building in the given grid

		return gsdo.unreleasedMethod.get(buildingNumber); // Get voltage for this building's node/bus
	}

	public double getLocalBusCurrent(UnreleasedClass gsdo) {

		String uuid = unreleasedMethod.unreleasedMethod.unreleasedMethod.toString();
		uuid = uuid.substring(uuid.length() - 4);
		Integer buildingNumber = Integer.parseInt(uuid) - 1; // Bus number of this building in the given grid

		return Math
				.sqrt(Math.pow(gsdo.unreleasedMethod.get(buildingNumber), 2)
						+ Math.pow(gsdo.unreleasedMethod.get(buildingNumber), 2))
				/ gsdo.unreleasedMethod.get(buildingNumber);
	}

	public double getLocalBusActivePower(UnreleasedClass gsdo) {

		String uuid = unreleasedMethod.unreleasedMethod.unreleasedMethod.toString();
		uuid = uuid.substring(uuid.length() - 4);
		Integer buildingNumber = Integer.parseInt(uuid) - 1; // Bus number of this building in the given grid

		return gsdo.unreleasedMethod.get(buildingNumber);
	}

	public double getLocalBusReactivePower(UnreleasedClass gsdo) {

		String uuid = unreleasedMethod.unreleasedMethod.unreleasedMethod.toString();
		uuid = uuid.substring(uuid.length() - 4);
		Integer buildingNumber = Integer.parseInt(uuid) - 1; // Bus number of this building in the given grid

		return gsdo.unreleasedMethod.get(buildingNumber);
	}
}
