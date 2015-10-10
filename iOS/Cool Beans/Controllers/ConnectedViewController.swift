//
//  ConnectedViewController.swift
//  Cool Beans
//
//  Created by Kyle on 11/14/14.
//  Copyright (c) 2014 Kyle Weiner. All rights reserved.
//

import UIKit
import Foundation
import Alamofire





var someBytes = NSMutableData.init(capacity: 4);
var byteCounter = 0;
var concurrencyQueue:dispatch_queue_t{
    return dispatch_get_main_queue()
}


struct Temperature {
    enum State {
        case Unknown
        case Cold
        case Cool
        case Warm
        case Hot
    }
    
    var degreesCelcius: Float
    var degressFahrenheit: Float {
        return (degreesCelcius * 1.8) + 32.0
    }
    
    
    func state() -> State {
        let fahrenheit = degressFahrenheit
        switch Int(fahrenheit) {
        case let x where fahrenheit <= 39:
            return .Cold
        case 40...65:
            return .Cool
        case 66...80:
            return .Warm
        case let x where fahrenheit >= 81:
            return .Hot
        default:
            return .Unknown
        }
    }
}

class ConnectedViewController: UIViewController, PTDBeanDelegate {
    
    
    
    let refreshControl = UIRefreshControl()
    
    var connectedBean: PTDBean?
    var connectedBean2: PTDBean?
    var connectedBean3: PTDBean?
    var currentTemperature: Temperature = Temperature(degreesCelcius: 0.0) {
        didSet {
            updateTemperatureView()
            updateBean()
        }
    }
    
    @IBOutlet weak var scrollView: UIScrollView!
    @IBOutlet weak var temperatureView: TemperatureView!
    
    // MARK: Lifecycle
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        Alamofire.request(.GET, "http://152.23.11.115:3000/register").response { (request, response, data, error) in
                print(request)
                print(response)
                print(data)
                print(error)
        }
        
        let comment: [String:AnyObject] = [
            "comment": "my First Comment",
            "commentDate": "2014-05-13 14:30 PM",
            "isSigned": 1,
            "patientId": 2,
            "documentId": 3
        ]
        
        Alamofire.request(.POST, "http://152.23.11.115:3000/register", parameters: comment).responseJSON { response in debugPrint(response)
            print(response.request)
            print(response.response)
            print(response.data)
            print(response.result)
        }
        
        
        // Update the name label.
        temperatureView.nameLabel.text = connectedBean?.name
        
        // Add pull-to-refresh control.
        refreshControl.addTarget(self, action: "didPullToRefresh:", forControlEvents: .ValueChanged)
        refreshControl.tintColor = .whiteColor()
        scrollView.addSubview(refreshControl)
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        connectedBean?.readAccelerationAxes()
    }
    
    // MARK: Actions
    
    func didPullToRefresh(sender: AnyObject) {
        refreshControl.endRefreshing()
        //connectedBean?.readAccelerationAxes()
    }
    
    // MARK: PTDBeanDelegate
    
    func bean(bean: PTDBean!, didUpdateTemperature degrees_celsius: NSNumber!) {
        

    }
    
    func bean(bean: PTDBean!, didUpdateAccelerationAxes acceleration: PTDAcceleration){
        //print("x:%d y:%d z: %d", acceleration.x, acceleration.y, acceleration.z)
        temperatureView.temperatureLabel.text = String(format: "x:%d y:%d z: %d", acceleration.x, acceleration.y, acceleration.z)
    }
    
    
    /**************************************************************************************/
    func bean(bean: PTDBean!, serialDataReceived data: NSData!){
        //  concurrencyQueue =
        var g = 0;
        /**
        * All accelerometer data/timesync messages are 32 / 8 or 4 bytes
        * long.
        * The following code collects individual bytes and waits for chunks of
        * four to be ready for interpretation.
        */
        if (data.length == 1) {
            
            NSLog("Unhandled serial message: " + String(data))
            var hexString = String(data);
            
            var firstChar =  hexString.removeAtIndex(hexString.startIndex.advancedBy(1))
            var secondChar = hexString.removeAtIndex(hexString.startIndex.advancedBy(2))
            
            
            
            if (secondChar == "1"){
                g += 1*1
            }
            if (secondChar == "2"){
                g += 2*1
            }
            if (secondChar == "3"){
                g += 3*1
            }
            if (secondChar == "4"){
                g += 4*1
            }
            if (secondChar == "5"){
                g += 5*1
            }
            if (secondChar == "6"){
                g += 6*1
            }
            if (secondChar == "7"){
                g += 7*1
            }
            if (secondChar == "8"){
                g += 8*1
            }
            if (secondChar == "9"){
                g += 9*1
            }
            if (secondChar == "a"){
                g += 10*1
            }
            if (secondChar == "b"){
                g += 11*1
            }
            if (secondChar == "c"){
                g += 12*1
            }
            if (secondChar == "d"){
                g += 13*1
            }
            if (secondChar == "e"){
                g += 14*1
            }
            if (secondChar == "f"){
                g += 15*1
            }
            
            if (firstChar == "1"){
                g += 1*16
            }
            if (firstChar == "2"){
                g += 2*16
            }
            if (firstChar == "3"){
                g += 3*16
            }
            if (firstChar == "4"){
                g += 4*16
            }
            if (firstChar == "5"){
                g += 5*16
            }
            if (firstChar == "6"){
                g += 6*16
            }
            if (firstChar == "7"){
                g += 7*16
            }
            if (firstChar == "8"){
                g *= -1
            }
            if (firstChar == "9"){
                g += 16; g *= -1;
            }
            if (firstChar == "a"){
                g += 16*2; g *= -1;
            }
            if (firstChar == "b"){
                g += 16*3; g *= -1;
            }
            if (firstChar == "c"){
                g += 16*4; g *= -1;
            }
            if (firstChar == "d"){
                g += 16*5; g *= -1;
            }
            if (firstChar == "e"){
                g += 16*6; g *= -1;
            }
            if (firstChar == "f"){
                g += 16*7; g *= -1;
            }
            
            
            
            
            
            //if the char is numeric, leave it, else turn it into the right integer and multiply
            
            
            temperatureView.temperatureLabel.text = String(format: "%i", g)
            NSLog("Message Length: %i" ,data.length)
            
            return;
        }
        
        someBytes?.appendBytes(data.bytes, length: 1)
        byteCounter++;
        if (byteCounter == 4){
            byteCounter = 0;
            return;
            return;
        }
        
    }
    
    
    
    // MARK: Helper
    
    func updateTemperatureView() {
        var backgroundColor: UIColor
        
        switch currentTemperature.state() {
        case .Unknown:
            backgroundColor = .blackColor()
        case .Cold:
            backgroundColor = .CBColdColor()
        case .Cool:
            backgroundColor = .CBCoolColor()
        case .Warm:
            backgroundColor = .CBWarmColor()
        case .Hot:
            backgroundColor = .CBHotColor()
        }
        
        UIView.animateWithDuration(0.4, animations: {
            self.scrollView.backgroundColor = backgroundColor
            self.temperatureView.containerView.backgroundColor = backgroundColor
        })
    }
    
    func updateBean() {
        // connectedBean?.setLedColor(temperatureView.containerView.backgroundColor)
    }
    
    
}