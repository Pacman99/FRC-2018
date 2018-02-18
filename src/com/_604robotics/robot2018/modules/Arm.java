package com._604robotics.robot2018.modules;

import com._604robotics.robot2018.constants.Calibration;
import com._604robotics.robot2018.constants.Ports;
import com._604robotics.robotnik.Action;
import com._604robotics.robotnik.Input;
import com._604robotics.robotnik.Module;
import com._604robotics.robotnik.Output;
import com._604robotics.robotnik.prefabs.controller.RotatingArmPIDController;
import com._604robotics.robotnik.prefabs.devices.TalonPWMEncoder;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

public class Arm extends Module {

    private WPI_TalonSRX motorA = new WPI_TalonSRX(Ports.ARM_MOTOR_A);
    private WPI_TalonSRX motorB = new WPI_TalonSRX(Ports.ARM_MOTOR_B);
    public TalonPWMEncoder encoder = new TalonPWMEncoder(motorB);

    public final Setpoint setpoint = new Setpoint(Calibration.ARM_LOW_TARGET);

    public final Output<Double> encoderRate = addOutput("Arm Rate", encoder::getVelocity);
    public final Output<Double> encoderClicks = addOutput("Arm Clicks", encoder::getPosition);

    private final RotatingArmPIDController pid;
    
    public final Output<Double> pidError;
    
    public void resetIntegral(double sum) {
        pid.setErrorSum(sum);
    }

    public class Move extends Action {
        public final Input<Double> liftPower;

        public Move() {
            this(0);
        }

        public Move (double power) {
            super(Arm.this, Move.class);
            liftPower = addInput("Lift Power", power, true);
        }

        @Override
        public void run () {
            motorA.set(liftPower.get());
        }
    }

    public class Setpoint extends Action {
        public final Input<Double> target_clicks;

        public Setpoint() {
            this(0);
        }

        public Setpoint(double clicks) {
            super(Arm.this, Setpoint.class);
            target_clicks = addInput("Target Arm Clicks", clicks, true);
        }
        
        public boolean atTolerance() {
            return pid.onTarget();
        }

        @Override
        public void begin() {
            pid.enable();
        }
        @Override
        public void run () {
            pid.setSetpoint(target_clicks.get());
        }
        @Override
        public void end () {
            pid.disable();
        }
    }

    public Arm() {
        super(Arm.class);
        encoder.setInverted(true);
        encoder.setOffset(Calibration.ARM_ENCODER_ZERO);
        motorA.setInverted(false);
        motorB.setInverted(true);
        motorB.set(ControlMode.Follower,Ports.ARM_MOTOR_A);
        pid = new RotatingArmPIDController(Calibration.ARM_P,
                Calibration.ARM_I,
                Calibration.ARM_D,
                Calibration.ARM_F,
                encoder,
                motorA,
                Calibration.ARM_PID_PERIOD);
        pid.setInputRange(-Calibration.ARM_ENCODER_FULL_ROT/2,
                Calibration.ARM_ENCODER_FULL_ROT/2);
        pidError = addOutput("Arm PID Error", pid::getError);
        pid.setIntegralLimits(Calibration.ARM_MIN_SUM, Calibration.ARM_MAX_SUM);
        pid.setOutputRange(Calibration.ARM_MIN_SPEED, Calibration.ARM_MAX_SPEED);
        pid.setAbsoluteTolerance(Calibration.ARM_PID_TOLERANCE);
        //setpoint.target_clicks.set(encoder.getPosition());
        setDefaultAction(setpoint);
    }
}
