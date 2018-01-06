package com._604robotics.robot2018.modules;

import com._604robotics.robot2018.constants.Ports;
import com._604robotics.robotnik.Action;
import com._604robotics.robotnik.Input;
import com._604robotics.robotnik.Module;
import com._604robotics.robotnik.Output;

import edu.wpi.first.wpilibj.CounterBase;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.RobotDrive;

public class Drive extends Module {
    private final RobotDrive robotDrive = new RobotDrive(
            Ports.DRIVE_FRONT_LEFT_MOTOR,
            Ports.DRIVE_REAR_LEFT_MOTOR,
            Ports.DRIVE_FRONT_RIGHT_MOTOR,
            Ports.DRIVE_REAR_RIGHT_MOTOR);

    // Reversed from previously due to new mountings
    private final Encoder encoderLeft = new Encoder(Ports.ENCODER_LEFT_A,
            Ports.ENCODER_LEFT_B,
            true,
            CounterBase.EncodingType.k4X);
    private final Encoder encoderRight = new Encoder(Ports.ENCODER_RIGHT_A,
            Ports.ENCODER_RIGHT_B,
            false,
            CounterBase.EncodingType.k4X);
    
    //private final AnalogGyro horizGyro=new AnalogGyro(Ports.HORIZGYRO);
    
    public synchronized void resetSensors() {
        encoderLeft.reset();
        encoderRight.reset();
        //horizGyro.reset();
    }

    //public final Output<Double> gyroAngle = addOutput("gyroAngle",horizGyro::getAngle);
    public final Output<Integer> leftClicks = addOutput("leftClicks", encoderLeft::get);
    public final Output<Integer> rightClicks = addOutput("rightClicks", encoderRight::get);
    
    public final Output<Double> leftClickRate = addOutput("leftClickRate", encoderLeft::getRate);
    public final Output<Double> rightClickRate = addOutput("rightClickRate", encoderRight::getRate);

    public class Idle extends Action {
        public Idle () {
            super(Drive.this, Idle.class);
        }

        @Override
        public void run () {
            robotDrive.stopMotor();
        }
    }

    public final Action idle = new Idle();

    public class TankDrive extends Action {
        public final Input<Double> leftPower;
        public final Input<Double> rightPower;
        public final boolean squared;

        public TankDrive () {
            this(0, 0, true);
        }
        
        public TankDrive (boolean squared) {
            this(0, 0, squared);
        }

        public TankDrive (double defaultLeftPower, double defaultRightPower) {
            this(defaultLeftPower, defaultRightPower, true);
        }

        public TankDrive (double defaultLeftPower, double defaultRightPower, boolean squared) {
            super(Drive.this, TankDrive.class);
            // Make these inputs persistent for auton code
            leftPower = addInput("leftPower", defaultLeftPower, true);
            rightPower = addInput("rightPower", defaultRightPower, true);
            this.squared = squared; 
        }

        @Override
        public void run () {
            robotDrive.tankDrive(leftPower.get(), rightPower.get(), squared);
        }
    }

    public class ArcadeDrive extends Action {
        public final Input<Double> movePower;
        public final Input<Double> rotatePower;
        public final boolean squared;

        public ArcadeDrive () {
            this(0, 0, true);
        }
        
        public ArcadeDrive (boolean squared) {
            this(0, 0, squared);
        }

        public ArcadeDrive (double defaultMovePower, double defaultRotPower) {
            this(defaultMovePower, defaultRotPower, true);
        }

        public ArcadeDrive (double defaultMovePower, double defaultRotPower, boolean squared) {
            super(Drive.this, ArcadeDrive.class);
            // Make these inputs persistent for auton code
            movePower = addInput("movePower", defaultMovePower, true);
            rotatePower = addInput("rotatePower", defaultRotPower, true);
            this.squared = squared;
        }

        @Override
        public void run () {
            robotDrive.arcadeDrive(movePower.get(), rotatePower.get(), squared);
        }
    }

    public Drive () {
        super(Drive.class);
        //horizGyro.calibrate();
        setDefaultAction(idle);
    }
}