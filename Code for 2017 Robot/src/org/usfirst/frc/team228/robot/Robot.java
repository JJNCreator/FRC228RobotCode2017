package org.usfirst.frc.team228.robot;

import com.ctre.CANTalon; 
import com.ctre.CANTalon.FeedbackDevice;
import com.ctre.CANTalon.TalonControlMode;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.VictorSP; //not victor like we thought
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.Preferences;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot
{
	//PRE INIT - Create variables
	
	//ATTENTION!
	final boolean isPracticeRobot = false;
	
	Preferences robotPrefs;

	//Autonomous
	boolean inAuto;	//Checks if auto is on //removed "= true", because it shouldn't default to true
	Timer autoTimer;	//time elapsed while in autonomous
	int gearAutonCase = 0;
	double reachedTargetTime = 999;
	
	//Auto Selection
	final String defaultAuto = "Drive Forward";
	final String centerGearAuto = "Center Gear Auto";
	final String gyroAuto = "Gyro Test Auto";
	final String rightGearAuto = "Right Gear Auto";
	final String leftGearAuto = "Left Gear Auto";
	String autoSelected;	//user's autonomous selection
	SendableChooser<String> autoChooser;	//autonomous selector //new
	//SendableChooser autoChooser; //old
	
	//Teleop Drive Mode Selection
	final String arcadeMode = "Arcade";
	final String tankMode = "Tank";
	final String GTAMode = "GTA";	//Strings for each particular mode
	String driveMode;	//user's drive mode selection
	//int driveTrainId;
	SendableChooser<String> driveChooser;	//drive mode selector //old
	//SendableChooser driveChooser; //old

	PowerDistributionPanel pdPanel; 
			
	//Compressor
	Compressor compressor;
	
	//Gyro
	ADXRS450_Gyro robotGyro;
	boolean calGyro; //true = gyro will calibrate
	
	//Drivetrain
	VictorSP leftDrive1, leftDrive2, rightDrive1, rightDrive2;	//drive motor controllers
	Encoder leftDriveEncoder, rightDriveEncoder;	//encoders
	Solenoid driveShifter;
	boolean shifterLowGear, shifterButtonPrev;	//pneumatics (shifters)
		
	//Drive Function
	RobotDrive drivetrain;
	XboxController driverController, operatorController;	//driver: driving, operator: functions (like intake and shooter)
	double previousInput;	// for acceleration limit
	final double rateLimit = 2.0;
	//double currentInput;
	double previousTime;
	Timer teleopTimer;
	double gearDropTime = -1;
	double hangStopTime = -1;
	
	//Gear Manipulation
	DoubleSolenoid pincher;	//Pincher
	boolean pincherClosed; //true = pinching
	boolean pincherButtonPrev; //state of the button from last iteration
	Solenoid gearRotator; 	//Rotator, moves gear up/down
	boolean gearRotatorDown; //true = rotator down
	boolean gearRotatorButtonPrev; //state of the button from last iteration
	VictorSP gearRoller;	//Roller gear pickup (new mechanism)
	
	
	//Ball Manipulation
	VictorSP intakeBelt, feederBelt; //Feeder and Intake belt motor controllers
	double feederMaxSpeed; //user input constant to control the speed of feederBelt
	Solenoid HLGate;	//human load gate
	boolean HLGateOpen; //true = human load gate open
	boolean HLGateButtonPrev; //state of the button from last iteration
	Solenoid dumperGate;	//dumper gate
	boolean dumperGateOpen; //true = dumper gate open
	boolean dumperButtonPrev; //state of the button from last iteration

	//Shooter
	//controllers (Talons)
	CANTalon shooterMotor1,shooterMotor2,shooterMotor3;
	boolean shooterOn;
	boolean shooterButtonPrev;
	double OLShooterValue = 0.57; //constant for shooter speed; "open loop shooter value"
	double ShooterTarget, ShooterF, ShooterP, ShooterI, ShooterD;	//target value for shooter speed, and constants for FPID
	int ShooterIZone;
	Timer errorTimer;	//timer for how long error is below threshold
	double shooterErrorThreshold = 20.0;	//shooter speed error; feeder runs when under error for set time
	//double shooterErrorThreshold; //check robotinit for value
	double shooterTimeDelay;	//the amount of time the shooter speed must be within the error threshold to be considered stable
								//check robotinit for value
	
	//Hanging
	VictorSP hangingWinch;	//motor controllers
	boolean hangFeedForward; //when true, will apply feed forward value to hanger 
	boolean hangButtonPrev; //state of the button from last iteration
	double hangMotorLimit;	//threshold for the hanging motor current, over which the motor should be shut off
	
	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	public void robotInit()
	{
	//Robot Init - Put Things on SmartDashboard and Assign Everything
		
		//RoboRIO's preferences, allowing us to load or save values,
		//such as strings, booleans, doubles, integers, and floats
		//These values are stored right on the roboRIO
		robotPrefs = Preferences.getInstance();
				
		//Camera
		CameraServer.getInstance().startAutomaticCapture();
		
		//Compressor
		//compressor = new Compressor();
		
		//PDPanel
		pdPanel = new PowerDistributionPanel(5);
				
		//Power Distribution Panel
		pdPanel = new PowerDistributionPanel(4); //double check CAN ID on EACH robot	//couldn't find jake's code lol
		
		//Gyro
		robotGyro = new ADXRS450_Gyro();
		SmartDashboard.putBoolean("Gyro Calibrate", true);

		//Autonomous Chooser
		autoChooser = new SendableChooser<String>();	//Chooser for Autonomous programs
		//autoChooser = new SendableChooser();autoChooser.addDefault("Drive Forward", defaultAuto);
		autoChooser.addObject("Center Gear", centerGearAuto);
		autoChooser.addObject("Right Gear", rightGearAuto);
		autoChooser.addObject("Left Gear", leftGearAuto);
		autoChooser.addObject("Gyro Test Auto", gyroAuto); //put options
		SmartDashboard.putData("Auto Choices", autoChooser);

		//Teleop Drive Mode Chooser
		driveChooser = new SendableChooser<String>();	//Chooser for Teleop Drive Mode
		//driveChooser = new SendableChooser();
		driveChooser.addDefault("GTA Drive", GTAMode);
		driveChooser.addObject("Tank Drive", tankMode); 
		driveChooser.addObject("Arcade Drive", arcadeMode); //put options
		SmartDashboard.putData("Drive Choices", driveChooser);

		//Autonomous mode timer
		autoTimer = new Timer();
		
		//Teleop mode timer
		teleopTimer = new Timer();
		
		//Drivetrain
		leftDrive1 = new VictorSP(0);
		leftDrive2 = new VictorSP(1);
		rightDrive1 = new VictorSP(2);
		rightDrive2 = new VictorSP(3);	//DRIVE MOTOR CONTROLLERS
		driveShifter = new Solenoid(0);
		shifterLowGear = false; //false means high gear
		shifterButtonPrev = false;						//SHIFTERS
		leftDriveEncoder = new Encoder(0, 1, false); //the boolean is to indicate if it is backward
		rightDriveEncoder = new Encoder(2, 3, true); //one should always be backward of the other
		leftDriveEncoder.setDistancePerPulse(0.0222); //one pulse is 1/45 of an inch
		rightDriveEncoder.setDistancePerPulse(0.0222);	//ENCODERS

		//Drive Function
		drivetrain = new RobotDrive(leftDrive1, leftDrive2, rightDrive1, rightDrive2);
		SmartDashboard.putNumber("Acceleration Limit", rateLimit);	//Put drive acceleration limit on SDB
		driverController = new XboxController(0);
		operatorController = new XboxController(1);	//XboxControllers
		
		//Gear Manipulation
		pincher = new DoubleSolenoid(2,3);	//Pincher
		pincherClosed = true; //this makes setting up auto easier
		pincherButtonPrev = false;
		gearRotator = new Solenoid(4);	//Rotator
		gearRotatorDown = false;
		gearRotatorButtonPrev = false;
		gearRoller = new VictorSP(7);
		
		//Ball Manipulation
		intakeBelt = new VictorSP(4); //ball intake
		feederBelt = new VictorSP(5); //shooter feed belt
		feederMaxSpeed = 1.0; //arbitrary starting value
		SmartDashboard.putNumber("Feeder Max Speed", feederMaxSpeed); //put feederMaxSpeed on SDB
		HLGate = new Solenoid(5);	//Human Load gate
		HLGateOpen = false;
		HLGateButtonPrev = false;
		dumperGate = new Solenoid(6);	//Dumper Gate
		dumperGateOpen = false;
		dumperButtonPrev = false;
		
		//Shooter
		//Assign Shooter motor controllers
		shooterMotor1 = new CANTalon(1); //verify IDs
		shooterMotor2 = new CANTalon(2); 
		shooterMotor3 = new CANTalon(3);
		errorTimer = new Timer();	//timer for how long error is below threshold
		shooterMotor3.setFeedbackDevice(FeedbackDevice.QuadEncoder);
		//set other Talons to follow - verify correct numbers for everything
		shooterMotor2.changeControlMode(CANTalon.TalonControlMode.Follower);
		shooterMotor2.set(shooterMotor3.getDeviceID());
		shooterMotor1.changeControlMode(CANTalon.TalonControlMode.Follower);
		shooterMotor1.set(shooterMotor3.getDeviceID());
		//set nominal and peak voltage, 12V means full (but only here!)
		shooterMotor3.configNominalOutputVoltage(0.0, -0.0);
		shooterMotor3.configPeakOutputVoltage(12.0, -12.0);
		if (isPracticeRobot)
		{
			shooterMotor3.reverseSensor(true);
		}
		//FPID, Target AND IZONE SETTINGS 
		// These aggressive PID settings are what we tried last.
		//Shooter Target setting for point-blank shot. Was 195 until recently
		ShooterTarget = 190;
		ShooterF = 2.9;
		ShooterP = 10;
		ShooterI = 0.04; //was 0.03
		ShooterD = 300; //was 260, 450, 400
		ShooterIZone = 5;
		//display target rpm, F, P, I, and D
		SmartDashboard.putNumber("Shooter Target", ShooterTarget);
		SmartDashboard.putNumber("Shooter F", ShooterF);
		SmartDashboard.putNumber("Shooter P", ShooterP);
		SmartDashboard.putNumber("Shooter I", ShooterI);
		SmartDashboard.putNumber("Shooter D", ShooterD);
		SmartDashboard.putNumber("Shooter I Zone", ShooterIZone);
		shooterMotor3.setProfile(0); //Select which closed loop profile to use, 
		//and uses whatever PIDF gains and the such that are already there.
		shooterMotor3.setF(ShooterF);
		shooterMotor3.setP(ShooterP);
		shooterMotor3.setI(ShooterI);
		shooterMotor3.setD(ShooterD);
		shooterMotor3.setIZone(ShooterIZone);
		shooterButtonPrev = false;
		shooterOn = false;
		shooterErrorThreshold = 20.0;	//the feeder will run when under this error for a set time
		shooterTimeDelay = 0.05;	//amount of time the shooter speed must be within the error threshold
											//to be considered stable
		SmartDashboard.putNumber("Shooter constant", OLShooterValue);	//put shooter constant data
		//get user input for constant, assign to OLShooterValue
		SmartDashboard.putBoolean("Shooter PID on", true);	//boolean (which becomes checkbox)
		SmartDashboard.putNumber("Shooter Speed Error Threshold", shooterErrorThreshold); //threshold for to check to run feeder
		SmartDashboard.putNumber("Shooter Time Delay", shooterTimeDelay);	//put shooter time delay on SDB
		
		//Hanger
		hangingWinch = new VictorSP(6);	//Assign Hanging motor controller
		hangingWinch.setInverted(true);
		hangFeedForward = false;
		hangButtonPrev = false;
		hangMotorLimit = 50.0;	//hanging motor current limit; motor is on a 40 amp breaker
		SmartDashboard.putNumber("Hang Motor Current Limit", hangMotorLimit);	//put hang motor limit on SDB
	
	}
	
	/**
	 * This function is called each time a new packet is received from the driver station
	 * (approximately every 20ms).
	 * Periodic code for all robot modes should go here.
	 */
	public void robotPeriodic()
	{
		//Put mechanism on/off values on SDB
		SmartDashboard.putBoolean("Drivetrain Low Gear", shifterLowGear);
		SmartDashboard.putBoolean("Pincher Closed", pincherClosed);
		SmartDashboard.putBoolean("Gear Down", gearRotatorDown);
		SmartDashboard.putBoolean("Human Gate Open", HLGateOpen);
		SmartDashboard.putBoolean("Dumper Open", dumperGateOpen);
		SmartDashboard.putBoolean("Shooter", shooterOn);
		SmartDashboard.putBoolean("Winch Feed Forward", hangFeedForward);
		//put on SDB whether it's set to practice robot
		SmartDashboard.putBoolean("Practice Robot Mode", isPracticeRobot);
		
		//checks if gyro calibration check box is on
		calGyro = SmartDashboard.getBoolean("Gyro Calibrate", true);
	}
	
	/**
	 * This code runs at the beginning of autonomous mode
	 */
	public void autonomousInit()
	{
		autoSelected = autoChooser.getSelected();
		System.out.println("Auto selected: " + autoSelected);	//get and print Autonomous selection
		robotGyro.reset();	//zeros gyro. also ensures gyro finishes calibrating before continuing.
		leftDriveEncoder.reset();
		rightDriveEncoder.reset();	//reset encoders
		autoTimer.reset();
		autoTimer.start();	//starting the timer after the gyro ensures auto runs for correct length even if delayed
	}

	/**
	 * This function is called periodically during autonomous
	 * The switch case will determine which autonomous mode runs based on the chooser
	 */
	public void autonomousPeriodic()
	{
		switch(autoSelected)
		{
		case centerGearAuto:
			gearAuto("Center");   
			break;
		case rightGearAuto:
			gearAuto("Right");
			break;
		case leftGearAuto:
			gearAuto("Left");
			break;
		case gyroAuto:
			gyroTestAuto();
			break;
		case defaultAuto:
			driveForwardAuto(80);
			break; //80 inches is clearly forward of center peg
		default:
			drivetrain.arcadeDrive(0.0, 0.0); //prevents watchdog error
			break;
		}
	}
	
	/**This just drives forward for a set number of seconds.
	* Never Tested!!!
	*/
	public void driveForwardAuto (double driveDistance)
	{
		final double gyroP = -0.195;
		double averageDistance;
		driveShifter.set(true);
		
		//if below code is uncommented, please comment this out
		averageDistance = (leftDriveEncoder.getDistance() + rightDriveEncoder.getDistance()) / 2;
		System.out.println(averageDistance);
		//error handling for encoder - if one encoder is totally dead, autonomous won't totally fail
		//TEST ME BEFORE UNCOMMENTING
		/*
		if (leftDriveEncoder.getDistance() == 0 && rightDriveEncoder.getDistance() > 6)
		{
			averageDistance = rightDriveEncoder.getDistance();
		}
		else if (rightDriveEncoder.getDistance() == 0 && leftDriveEncoder.getDistance > 6)
		{
			averageDistance = leftDriveEncoder.getDistance();
		}
		else
		{
			averageDistance = (leftDriveEncoder.getDistance() + rightDriveEncoder.getDistance()) / 2;
		}
		*/
		switch (gearAutonCase) //yes I'm bad and should make a new variable
		{
		case 0:
			if (averageDistance < driveDistance)
			{
				drivetrain.arcadeDrive(-0.7, robotGyro.getAngle() * gyroP);
			}
			else
			{
				gearAutonCase++;
			}
			break;
		case 1:
		default:
			drivetrain.arcadeDrive(0, 0);
			break;
		}
	}
	
	
	/**This scores a gear on a peg directly in front of the robot.
	*/
	public void gearAuto(String sideOfField)
	{
		final double gyroP = -0.195; //proportional gain constant for gyro, will need tuning
		final double targetDistance1 = 69; //distance for side autons to drive before turning
		final double targetAngleRight = -60; //turn angle for right hand auto
		final double targetAngleLeft = 60; //turn angle for left hand auto, should be inverse of right
		final double targetDistanceTwo = 69; //was 74, then 72. SUBTRACTED 3 inches to account for new mechanism
		double targetDistance2; //variable used to figure out distance of straight line drive to peg
		double averageDistance; //calculated distance based on encoder readout
		
		//the side autos drive forward for a slightly shorter distance
		//the adjustment is made here so it can be tweaked easily
		if (sideOfField == "Left" || sideOfField == "Right")
		{
			targetDistance2 = targetDistanceTwo - 4; //was 6, adjusted based on adjusting two
		}
		else
		{
			targetDistance2 = targetDistanceTwo;
		}
		
		driveShifter.set(true); // run autonomous mode in low gear
		
		//uncomment this line if error handling code does not work
		averageDistance = (leftDriveEncoder.getDistance() + rightDriveEncoder.getDistance()) / 2;
		
		//error handling for encoder - if one encoder is totally dead, autonomous won't totally fail
		//TEST ME BEFORE COMPETITION
		if (leftDriveEncoder.getDistance() == 0 && rightDriveEncoder.getDistance() > 6)
		{
			averageDistance = rightDriveEncoder.getDistance();
		}
		else if (rightDriveEncoder.getDistance() == 0 && leftDriveEncoder.getDistance() > 6)
		{
			averageDistance = leftDriveEncoder.getDistance();
		}
		else
		{
			averageDistance = (leftDriveEncoder.getDistance() + rightDriveEncoder.getDistance()) / 2;
		}
		
		switch(gearAutonCase)
		{
		case 0: //check which direction you're going
			if (sideOfField == "Left" || sideOfField == "Right")
			{
				gearAutonCase++; //case 1 is first step of side case
			}
			else
			{
				gearAutonCase += 10; //case 2 is center gear auto case
				//the case 1 step drives the left / right cases forward
				//and turns them until they are in the center gear position
			}
			break;
		case 1: //for left-right auto, go straight out to rotation point
				//this is an area that can be sped up, but play with setpoints if you do
			if (averageDistance < targetDistance1)
			{
				drivetrain.arcadeDrive(-0.7, robotGyro.getAngle() * gyroP);
			}
			else if (averageDistance > targetDistance1 + 4)
			{
				drivetrain.arcadeDrive(0.7, robotGyro.getAngle() * gyroP);
			}
			else
			{
				gearAutonCase++;
			}
			break;
		case 2: //spin a certain angle
		//i am pretty sure i could make a variable changing the sign of getAngle to make this work as 1 call 
		//but that would require testing time we just don't have at the moment
			if (sideOfField == "Right") //if sideOfField == "Right"
			{
				System.out.println(robotGyro.getAngle());
				if (robotGyro.getAngle() > targetAngleRight)
				{
					drivetrain.arcadeDrive(0, -0.75);
				}
				else if (robotGyro.getAngle() < targetAngleRight - 1) //1 is the overshoot allowance
				{
					drivetrain.arcadeDrive(0, +0.75); //consider tuning this down a little
				}
				else
				{
					System.out.println("Got to this part");
					robotGyro.reset();
					leftDriveEncoder.reset();
					rightDriveEncoder.reset();
					drivetrain.arcadeDrive(0, 0);
					gearAutonCase = 10; //advance to "center gear" code
				}
			}
			if (sideOfField == "Left")
			{
				System.out.println(robotGyro.getAngle());
				if (robotGyro.getAngle() < targetAngleLeft)
				{
					drivetrain.arcadeDrive(0, 0.75);
				}
				else if (robotGyro.getAngle() > targetAngleLeft + 1) //1 is the overshoot allowance
				{
					drivetrain.arcadeDrive(0, -0.75); //consider tuning this down a little
				}
				else
				{
					System.out.println("Got to this part");
					robotGyro.reset(); //ensures we are starting at zero for straight line auto
					leftDriveEncoder.reset();
					rightDriveEncoder.reset();
					drivetrain.arcadeDrive(0, 0);
					gearAutonCase = 10; //advance to "center gear" auto code
				}
			}
			break;
		case 10: //drive to target distance straight forward
			if (averageDistance < targetDistance2)
			{
				drivetrain.arcadeDrive(-0.7, robotGyro.getAngle() * gyroP);
			}
			else if (averageDistance > targetDistance2 + 4) //4 is the overshoot allowance
			{
				drivetrain.arcadeDrive(0.7, robotGyro.getAngle() * gyroP);
				//currently this else case is rarely triggered, because the code will advance in the instants before overshooting occurs
			}
			else
			{
				drivetrain.arcadeDrive(0, 0);
				reachedTargetTime = autoTimer.get();
				gearAutonCase++; //move on to the next round!
			}
			break;
		case 11: //after waiting 0.25 second, drop the thing, after 0.5 advance
			if (autoTimer.get() > reachedTargetTime + 0.25)
			{
				//pincher.set(DoubleSolenoid.Value.kReverse);
				gearRotator.set(true); //drop gear intake
				gearRoller.set(-0.25); //reverse gear intake
			}
			if (autoTimer.get() > reachedTargetTime + 0.5)
			{
				gearAutonCase++; //advance to drive backwards step
			}
			break;
		case 12: //drive backwards after dropping gear
			drivetrain.arcadeDrive(0.7, robotGyro.getAngle() * gyroP);
			if (autoTimer.get() > reachedTargetTime + 2)
			{
				gearRotator.set(false); //bring gear rotator back up
				gearRoller.set(0); //stop gear roller
				gearAutonCase++; //advance to stop driving step
			}
			break;
		case 13: //stop moving, if there is time add a 
		default:
			drivetrain.arcadeDrive(0, 0);
			break;
		}
	}
	
	/**
	 * This method checks the angle on the gyro repeatedly.
	 * It uses a delay, which I don't like and shouldn't be done in real code
	 */
	public void gyroTestAuto()
	{
	
		drivetrain.arcadeDrive(0.0, 0.0);	//turn off drivetrain
		System.out.println(robotGyro.getAngle());	//check gyro
		System.out.println(leftDriveEncoder.get());
		System.out.println(rightDriveEncoder.get());
		Timer.delay(0.1);	//wait .1s
	}
	
	/**
	 * This function is called once at the beginning of operator control
	 */
	public void teleopInit()
	{
		//driveMode = (String) driveChooser.getSelected();	//get drive mode selection (tank, arcade, GTA)
		//System.out.println("Drive mode selected: " + driveMode);	//print drive mode selection
		teleopTimer.reset();
		teleopTimer.start();
		previousTime = 0;
		previousInput = 0;
	}

	/**
	 * This function is called periodically during operator control
	 */
	public void teleopPeriodic()
	{
		//DRIVER CONTROLS
		double combinedTriggerValue;	//Value for the GTA Mode arcade function and SmartDashboard data
		double arcadeLeftStick;
		double tankLeftStick;
		double tankRightStick;	//for the "go" axis of arcade drive and axes of tank drive. this lets us square the values properly
		final boolean accelerationLimiterOn = true;	//to disable the acceleration limiter if necessary
		gearRoller.set(operatorController.getRawAxis(5));	//temp code for roller
		
		driveMode = (String)driveChooser.getSelected();		//Assign drive mode selection to driveMode
		switch(driveMode)	//Start Drive Mode
		{
		case arcadeMode:
			if (driverController.getRawAxis(1) >= 0)
			{
				arcadeLeftStick = Math.pow(driverController.getRawAxis(1), 2);	//square the value of the left axis. 
			}
			else
			{
				arcadeLeftStick = (-1 * Math.pow(driverController.getRawAxis(1), 2)); //if negative, it must be multiplied by -1 after squaring
			}
			drivetrain.arcadeDrive(arcadeLeftStick, driverController.getRawAxis(4));	//start arcade mode
			shifterControl(driverController.getRawButton(5));	//since we aren't in GTA mode, the left bumper controls shifting
			break;
			
		case tankMode:
			if (driverController.getRawAxis(1) >= 0)
			{
				tankLeftStick = Math.pow(driverController.getRawAxis(1), 2);	//square the axis value
			}
			else
			{
				tankLeftStick = (-1 * Math.pow(driverController.getRawAxis(1), 2)); //if negative, multiply by -1 after squaring
			}

			if (driverController.getRawAxis(5) >= 0)
			{
				tankRightStick = Math.pow(driverController.getRawAxis(5), 2);	//same for right axis
			}
			else
			{
				tankRightStick = (-1 * Math.pow(driverController.getRawAxis(5), 2));
			}
			drivetrain.tankDrive(tankLeftStick, tankRightStick);	//do tank mode
			//drivetrain.tankDrive(driverController, 1, driverController, 5);	//the old, simple tank mode
			shifterControl(driverController.getRawButton(5));	//since we aren't in GTA mode, the left bumper controls shifting
			break;
			
		case GTAMode:
		default: //we needed a default case to prevent watchdog errors if smartdashboard didn't work
			combinedTriggerValue = (-1 * Math.pow(driverController.getRawAxis(2), 2) + Math.pow(driverController.getRawAxis(3), 2));
			shifterControl(driverController.getAButton());
			if(driveShifter.get() == true || !accelerationLimiterOn)
			{
				drivetrain.arcadeDrive(combinedTriggerValue, driverController.getRawAxis(0));
				//if we are in GTA mode, shifting is assigned to a face button instead
				//SmartDashboard.putNumber("GTADriveValue", combinedTriggerValue);	//display combinedTriggerValue
			}
			else
			{
				drivetrain.arcadeDrive(rateLimiter(combinedTriggerValue), driverController.getRawAxis(0));
	
			}
			break;
		}
		
		//OPERATOR CONTROLS
		//Pincher: toggle  with B (DoubleSolenoid.Value.kForward = closed, ...kReverse = open)
		pincherControl(operatorController.getBButton());

		//Gear Control: toggle with A (on/down)/(DEFAULT: off/up); right stick Y axis for intake: up = in
		gearControl(operatorController.getAButton(), operatorController.getRawAxis(5));
		
		//Ball intake and feed combined function, left trigger and left joystick y axis
		intakeAndFeedBalls(operatorController.getRawAxis(2), operatorController.getRawAxis(1));

		//Human Load Gate: toggle with Y (on/open)/(DEFAULT: off/closed)
		HLGateControl(operatorController.getYButton());
		
		//Dumper Gate: toggle with right bumper (6) (on/open/shoot)/(DEFAULT: off/closed/no shoot)
		dumperGateControl(operatorController.getRawButton(6));

		//Shooter: spin up/down toggle with left bumper
		shooterControl(operatorController.getRawButton(5), SmartDashboard.getBoolean("Shooter PID on", false)); //set to true for PID

		//Hanging: right trigger passes for throttle value, X button toggles feed-forward on and off
		//TEMPORARILY CHANGED TO RIGHT STICK with 5 (3 is right button)
		hangingControl(operatorController.getRawAxis(3), operatorController.getXButton());
	}
	
	public void disabledInit()
	{

		//pincherClosed = false; //pincherClosed should not change actually
		gearRotatorDown = false;
		HLGateOpen = false;
		dumperGateOpen = false;
		shooterOn = false;		//set all of the toggle states for everything to false; this prevents unexpected movement on re-enable
		//shifterLowGear = false;
		//hangFeedForward = false; //also not sure here
		
		//in case the robot was in test mode, shooter motor 1 and 2 may not be in follower mode anymore
		//to be safe, they are set to follower mode every time the robot is disabled
		shooterMotor2.changeControlMode(CANTalon.TalonControlMode.Follower);
		shooterMotor2.set(shooterMotor3.getDeviceID());
		shooterMotor1.changeControlMode(CANTalon.TalonControlMode.Follower);
		shooterMotor1.set(shooterMotor3.getDeviceID());
		
		reachedTargetTime = 999;
		gearAutonCase = 0;	//set auton variables back to default; clean up later
	}
	
	/**
	 * This function is called periodically during disabled mode
	 */
	public void disabledPeriodic()
	{
		if (calGyro)	//if "Gyro Calibrate" is checked
		{
			robotGyro.calibrate();	//calibrate the gyro
		}
		//reachedTargetTime = 999; // bug fix for auto ask chris sorry
	}

	/**
	 * This function will limit how fast the robot can travel.  Returns a double
	 * @param currentInput
	 * @return the limit
	 */
	public double rateLimiter(double currentInput) {
		double speedRate;
		double currentTime = teleopTimer.get();
		double rateLimit = 2.0;

		rateLimit = SmartDashboard.getNumber("Acceleration Limit", rateLimit);	//update rateLimit from SDB
		
		speedRate = (currentInput - previousInput) / (currentTime - previousTime);
		
		if(speedRate > rateLimit) {
			currentInput = previousInput + rateLimit * (currentTime - previousTime);
		}
		else if (speedRate < (-1) * rateLimit) {
			currentInput = previousInput - rateLimit * (currentTime - previousTime);
		}
		return currentInput;
	}
	
	/**
	 * This function toggles the gear pincher open/closed using a button
	 * @param pincherButton
	 */
	public void pincherControl(boolean pincherButton)
	{
		if (pincherButton && pincherButton != pincherButtonPrev)	//if the button is pressed, and if it changed state
		{ 
			pincherClosed = !pincherClosed;	//toggle the state of pincher
		}
		pincherButtonPrev = pincherButton;	//now that check is complete, store value of pincherButton for next iteration of loop

		if (pincherClosed)
		{
			pincher.set(DoubleSolenoid.Value.kForward);	//ON: pinching/closed
		}
		else
		{
			pincher.set(DoubleSolenoid.Value.kReverse);	//OFF: release/open
		}
	}

	/**
	 * This function toggles the gear rotator up/down using a button
	 * and runs the gear intake roller
	 * @param gearRotatorButton
	 */
	public void gearControl(boolean gearRotatorButton, double intakeSpeed)
	{
		if (gearRotatorButton && gearRotatorButton != gearRotatorButtonPrev)	//if the button is pressed, and if it changed state
		{ 
			gearRotatorDown = !gearRotatorDown;	//toggle the state of gearRotator
			if (gearRotatorDown) //if gear is being dropped
			{
				gearDropTime = teleopTimer.get();	//set time of gear drop 
			}
		}
		gearRotatorButtonPrev = gearRotatorButton;	//now that check is complete, store value of gearRotatorButton for next iteration of loop

		if (gearRotatorDown)
		{
			gearRotator.set(true);	//ON: gear rotator down
		}
		else
		{
			gearRotator.set(false);	//OFF: gear rotator up
		}
		
		if (intakeSpeed <-0.25) //if less than -.25, set to -.25, to not shoot gear out too fast
		{
			gearRoller.set(-0.25);	
		}
		else if (gearDropTime + 0.5 > teleopTimer.get())	//if the gear roller is being dropped
		{
			gearRoller.set(-0.25); //reverse roller for 1 second
		}
		else
		{
			gearRoller.set(intakeSpeed);	//run the intake (joystick up = in)
		}
	}	
	
	/**
	* This function is a copy of both the intake and feed functions, put together.
	* This prevents conflicts between the two.
	* When you get time later, overload this function with a one-constructor version
	* intakeSpeed and feedSpeed are values based on the joystick input
	* for intake and feeding respectively
	*/
	public void intakeAndFeedBalls(double intakeSpeed, double feedSpeed)
	{
		feederMaxSpeed = SmartDashboard.getNumber("Feeder Max Speed", feederMaxSpeed);	//get user input for Feeder Max Speed
		shooterErrorThreshold = SmartDashboard.getNumber("Shooter Speed Error Threshold", shooterErrorThreshold);
														//get error threshold for shooter speed to check to run feeder
		
		//eliminates deadband caused by cheapo xbox controllers
		if (intakeSpeed > -0.2 && intakeSpeed < 0.2) 
		{
			intakeSpeed = 0;
		}
		if (feedSpeed > -0.2 && feedSpeed < 0.2)
		{
			feedSpeed = 0;
		}
		
		if (SmartDashboard.getBoolean("Shooter PID on", false) && shooterOn)	//if PID is on and shooter is on
		{

			if (mechanismStable(shooterMotor3.getClosedLoopError() < shooterErrorThreshold
					&& shooterMotor3.getClosedLoopError() > (shooterErrorThreshold*-1)))	//if shooter is stable at correct speed
			{
				feederBelt.set((-1*(intakeSpeed + feedSpeed))*feederMaxSpeed);	//run the feeder
				intakeBelt.set(intakeSpeed + (-1 * feedSpeed));	//run the intake
			}
			else
			{
				feederBelt.set(0);	//don't run feeder
				intakeBelt.set(0);	//don't run the intake
			}
		}
		else	//if not PID or shooter not on
		{
			feederBelt.set((-1*(intakeSpeed + feedSpeed))*feederMaxSpeed);	//run the feeder
			intakeBelt.set(intakeSpeed + (-1 * feedSpeed));	//run the intake
		}

	}

	/**
	 * This function toggles the human load gate using a button
	 * @param HLGateButton
	 */
	public void HLGateControl(boolean HLGateButton)
	{
		if (HLGateButton && HLGateButton != HLGateButtonPrev)	//if the button is pressed, and if it changed state
		{ 
			HLGateOpen = !HLGateOpen;	//toggle the state of HLGate
		}
		HLGateButtonPrev = HLGateButton;	//now that check is complete, store value of HLGateButton for next iteration of loop

		if (HLGateOpen)
		{
			HLGate.set(true);	//ON: open/no shoot state
		}
		else
		{
			HLGate.set(false);	//OFF: closed/shoot state
		}
	}

	/**
	 * This function toggles the dumper gate using a button
	 * @param dumperButton
	 */
	public void dumperGateControl(boolean dumperButton)
	{
		if (dumperButton && dumperButton != dumperButtonPrev)	//if the button is pressed, and if it changed state
		{ 
			dumperGateOpen = !dumperGateOpen;	//toggle the state of dumperGate
		}
		dumperButtonPrev = dumperButton;	//now that check is complete, store value of dumperButton for next iteration of loop

		if (dumperGateOpen)
		{
			dumperGate.set(true);	//dumperGate open/no shoot state
		}
		else
		{
			dumperGate.set(false);	//dumperGate closed/shoot state
		}
	}
	
	/**
	 * This function toggles the drivetrain shifters using a button
	 * We will probably also call this in autonomous
	 * @param shifterButton
	 */
	
	public void shifterControl(boolean shifterButton)
	{
		if (shifterButton && shifterButton != shifterButtonPrev)	//if button is pressed and the state changed (button isn't being held)
		{
			shifterLowGear = !shifterLowGear;	//toggle the shifter state (false means high gear!)
		}
		shifterButtonPrev = shifterButton;	//now that the check is complete, store button value for next loop
		
		if (shifterLowGear)
		{
			driveShifter.set(true);	//set to low gear
		} 
		else 
		{
			driveShifter.set(false);	//set to high gear
		}
	}
	
	/**
	 * This function handles the shooter motors on the 2017 robot.
	 * @param shooterButton, isPID
	 */
	public void shooterControl(boolean shooterButton, boolean isPID)
	{
		//Graph Signal and Error on SmartDashboard. Display Speed as number
		SmartDashboard.putNumber("Shooter Signal", shooterMotor3.getOutputVoltage() / shooterMotor3.getBusVoltage());
		SmartDashboard.putNumber("Shooter Error", shooterMotor3.getClosedLoopError());
		SmartDashboard.putNumber("Shooter Speed", shooterMotor3.getSpeed());
		//move back if needed to inside PID code
		
		OLShooterValue = SmartDashboard.getNumber("Shooter constant", OLShooterValue);
										//get user input for constant, assign to OLShooterValue
		
		if(shooterButton && shooterButton != shooterButtonPrev) //if a new button press has started
		{
			shooterOn = !shooterOn; //toggle shooter on/off 
		}
		
		shooterButtonPrev = shooterButton;
		
		if (shooterOn) //if the shooter has toggled on
		{
			//SmartDashboard.putBoolean("Shooter On", true); //indicate on dash that shooter is on //this is now in robotPeriodic
			if (isPID) //if the PID check box on smartdashboard is on
			{
				//get latest constants from SmartDashboard
				ShooterTarget = SmartDashboard.getNumber("Shooter Target", ShooterTarget);
				ShooterF = SmartDashboard.getNumber("Shooter F", ShooterF);
				ShooterP = SmartDashboard.getNumber("Shooter P", ShooterP);
				ShooterI = SmartDashboard.getNumber("Shooter I", ShooterI);
				ShooterD = SmartDashboard.getNumber("Shooter D", ShooterD);
				ShooterIZone = (int) SmartDashboard.getNumber("Shooter I Zone", ShooterIZone);
				
				//double ShooterTarget = 4000.0; //now a global variable
				shooterMotor3.changeControlMode(TalonControlMode.Speed);
				//encoder is 12 CPR (counts per revolution)
				//speed setpoint is ticks per 10ms

				//set PID loop to values from smart dashboard
				shooterMotor3.set(ShooterTarget);
				shooterMotor3.setF(ShooterF);
				shooterMotor3.setP(ShooterP);
				shooterMotor3.setI(ShooterI);
				shooterMotor3.setD(ShooterD);
				shooterMotor3.setIZone(ShooterIZone);
				
			}
			else //if PID is unchecked
			{
				shooterMotor3.changeControlMode(TalonControlMode.PercentVbus);	//change talon to open-loop mode
				shooterMotor3.set(OLShooterValue); //set a value; this value is from SmartDashboard
			}
		}
		else
		{
			//SmartDashboard.putBoolean("Shooter On", false); //like above, this is now in robotPeriodic
			shooterMotor3.changeControlMode(TalonControlMode.PercentVbus);	//change talon to open-loop mode
			shooterMotor3.set(0.0); //set to off
		}
	}
	
	/**This method will take a boolean (that is, if the absolute value error of the shooter
	 * is under a certain threshold), and if true (that is, the error is under that threshold),
	 * it will start a timer. If the timer reaches a certain amount of time (indicating the shooter
	 * is stable at the desired speed), the method will return true. If the timer has not reached
	 * that time, the method will return false. If the input boolean is false (that is, the error is
	 * over the threshold), the timer will stop, then reset, and the method will return false.
	 * 
	 * the return value of the method will be used to determine whether the shooter feed will run.
	 * @param isLowError
	 * @return
	 */
	public boolean mechanismStable(boolean isLowError)
	{
		SmartDashboard.getNumber("Shooter Time Delay", shooterTimeDelay);	//get time delay from SDB
		
		if (isLowError)	//if the error is below threshold
		{
			if (errorTimer.get() == 0.0)	//if the timer has not started (it will be 0)
			{
				errorTimer.start();
			}
			if (errorTimer.get() > shooterTimeDelay)	//if timer is under user input shooterTimeDelay
			{
				return true;	//run feeder
			}
			else	//if the error is above threshold and timer is not < 0.25s
			{
				return false;	//don't run feeder
			}
		}
		else	//if the error is not below threshold
		{
			errorTimer.stop();	//stop the timer
			errorTimer.reset();	//reset the timer
			return false;	//run the feeder
		}
	}
	
	/** 
	* This function controls the hanging winch and feed forward
	* hangingSpeed is expected to be between 0 and 1
	* This allows a trigger value to be passed into it, for example
	* If this is changed to a joystick, code will still work, but will allow hanger to run in reverse
	*
	* When the feed forward is enabled, hanger will stall at small voltage even without user input
	* This is designed to assist with scoring the bonus points at the end of the match
	*/
	public void hangingControl(double hangingSpeed, boolean ffButton)
	{
		final double hangFFValue = 0.3; //change this to change how much FF to apply
		hangMotorLimit = SmartDashboard.getNumber("Hanging Motor Current Limit", hangMotorLimit);	//get hang motor current limit from SDB
	
		if (pdPanel.getCurrent(5) > hangMotorLimit)	//if the current for the hanging motor is above the threshold
		{
			hangStopTime = teleopTimer.get(); //set overcurrent time 
		}
		
		if (hangStopTime + 1 > teleopTimer.get()) //if overcurrent tripped within last 1 second
		{
			hangingWinch.set(0.0); //set motor current to 0
		}
		
		else
		{
			//normal hang motor function
			if (ffButton != hangButtonPrev && ffButton) 	//check to see if the feed forward button has been pressed AND if it changed state
								//otherwise you would rapidly alternate between ff being on and off as long as the button was pressed!
				{ 
				hangFeedForward = !hangFeedForward;	//toggle the state of hang feed forward
			}
			hangButtonPrev = ffButton;	//now that check is complete, store value of button for next iteration of loop
			
			if (hangFeedForward)	//if feed forward is on
			{
				hangingSpeed += hangFFValue; //apply it to the input
			}
			if (hangingSpeed > 1.0) 	//if resulting value is >1.0
			{
				hangingSpeed = 1.0; //reduce it
			}
			
			hangingWinch.set(hangingSpeed);	//actually do the hanging motor command here
											//if you need to invert this, also invert the hangFFValue constant above
		}
	}

	/** Test mode is used in order to verify motors are working properly and spinning in correct direction.
	* Do not use for anything other than debugging!!
	*/
	//Currently Test mode is only used to make sure all 3 shooter motors are independently working
	public void testInit() {
		shooterMotor1.changeControlMode(TalonControlMode.PercentVbus);
		shooterMotor2.changeControlMode(TalonControlMode.PercentVbus);	//change motors 1 and 2 back to percent voltage mode
	}
	
	public void testPeriodic() {
		if (operatorController.getAButton())
		{
			shooterMotor1.set(1.0);
		}
		else
			shooterMotor1.set(0);
		if (operatorController.getBButton())
		{
			shooterMotor2.set(1.0);
		}
		else
		{
			shooterMotor2.set(0);
		}
		if (operatorController.getXButton())
		{
			shooterMotor3.set(1.0);
		}
		else
		{
			shooterMotor3.set(0);
		}
	}
}