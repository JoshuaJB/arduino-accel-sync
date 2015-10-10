//
//  DisconnectedViewController.swift
//  Cool Beans
//
//  Created by Kyle on 11/14/14.
//  Copyright (c) 2014 Kyle Weiner. All rights reserved.
//

import UIKit

class DisconnectedViewController: UIViewController, PTDBeanManagerDelegate {
    
    var beans = NSMutableDictionary.init(capacity: 3);
    
    let connectedViewControllerSegueIdentifier = "ViewConnection"
    
    var manager: PTDBeanManager!
    
    var connectedBean: PTDBean? {
        didSet {
            if  connectedBean == nil{
                beanManagerDidUpdateState(manager)
            } else {
                performSegueWithIdentifier(connectedViewControllerSegueIdentifier, sender: self)
            }
        }
    }
    
    var connectedBean2: PTDBean?{
        didSet{
            if connectedBean2 == nil {
                beanManagerDidUpdateState(manager)
            } else{
                performSegueWithIdentifier(connectedViewControllerSegueIdentifier, sender: self)
            }
        }
    }
    
    var connectedBean3: PTDBean?{
        didSet{
            if connectedBean2 == nil {
                beanManagerDidUpdateState(manager)
            } else{
                performSegueWithIdentifier(connectedViewControllerSegueIdentifier, sender: self)
            }
        }
    }
    
    @IBOutlet weak var activityIndicator: UIActivityIndicatorView!
    
    // MARK: Lifecycle
    
    override func viewDidLoad() {
        super.viewDidLoad()
        manager = PTDBeanManager(delegate: self)
    }
    
    // MARK: Navigation
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if segue.identifier == connectedViewControllerSegueIdentifier {
            let vc = segue.destinationViewController as! ConnectedViewController
            vc.connectedBean = connectedBean
            vc.connectedBean2 = connectedBean2
            vc.connectedBean3 = connectedBean3
            vc.connectedBean?.delegate = vc
        }
    }
    
    // MARK: PTDBeanManagerDelegate
    
    func beanManagerDidUpdateState(beanManager: PTDBeanManager!) {
        switch beanManager.state {
        case .Unsupported:
            UIAlertView(
                title: "Error",
                message: "This device is unsupported.",
                delegate: self,
                cancelButtonTitle: "OK"
                ).show()
        case .PoweredOff:
            UIAlertView(
                title: "Error",
                message: "Please turn on Bluetooth.",
                delegate: self,
                cancelButtonTitle: "OK"
                ).show()
        case .PoweredOn:
            beanManager.startScanningForBeans_error(nil);
        default:
            break
        }
    }
    /**************************************************************************************************/
    func beanManager(beanManager: PTDBeanManager!, didDiscoverBean bean: PTDBean!, error: NSError!) {
        let key = bean.identifier
        if ((self.beans.objectForKey(key)) == nil){
            //new bean
            beans.setObject(bean, forKey: key)
        }
        print("DISCOVERED BEAN \nName: \(bean.name), UUID: \(bean.identifier) RSSI: \(bean.RSSI)")
        if connectedBean == nil {
            if bean.state == .Discovered {
                manager.connectToBean(bean, error: nil)
            }
        }
        if connectedBean2 == nil {
            if bean.state == .Discovered {
                manager.connectToBean(bean, error: nil)
            }
        }
        if connectedBean3 == nil {
            if bean.state == .Discovered {
                manager.connectToBean(bean, error: nil)
            }
        }
        
    }
    /**************************************************************************************************/
    func BeanManager(beanManager: PTDBeanManager!, didConnectToBean bean: PTDBean!, error: NSError!) {
        print("CONNECTED BEAN \nName: \(bean.name), UUID: \(bean.identifier) RSSI: \(bean.RSSI)")
        if connectedBean == nil {
            connectedBean = bean
            return;
        }
        if connectedBean2 == nil {
            connectedBean2 = bean
            return;
        }
        if connectedBean3 == nil{
            connectedBean3 = bean
            return;
        }
    }
    
    func beanManager(beanManager: PTDBeanManager!, didDisconnectBean bean: PTDBean!, error: NSError!) {
        print("DISCONNECTED BEAN \nName: \(bean.name), UUID: \(bean.identifier) RSSI: \(bean.RSSI)")
        
        // Dismiss any modal view controllers.
        presentedViewController?.dismissViewControllerAnimated(true, completion: {
            self.dismissViewControllerAnimated(true, completion: nil)
        })
        
        self.connectedBean = nil
    }
    
}
