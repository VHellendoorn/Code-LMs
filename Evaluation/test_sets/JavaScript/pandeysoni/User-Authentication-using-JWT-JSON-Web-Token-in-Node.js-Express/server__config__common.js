'use strict';
const nodemailer = require("nodemailer")
const Config = require('./config')
const crypto = require('crypto')
const algorithm = 'aes-256-ctr'
const privateKey = Config.key.privateKey;

// create reusable transport method (opens pool of SMTP connections)
// console.log(Config.email.username+"  "+Config.email.password);
var smtpTransport = nodemailer.createTransport("SMTP", {
    service: "Gmail",
    auth: {
        user: Config.email.username,
        pass: Config.email.password
    }
});

exports.decrypt = (password) => {
   return decrypt(password);
};

exports.encrypt = (password) => {
    return encrypt(password);
};

exports.sentMailVerificationLink = (user, token, callback) => {
    var textLink = "http://"+Config.server.host+":"+ Config.server.port+"/"+Config.email.verifyEmailUrl+"/"+token;
    var from = `Pyrite Team<${Config.email.username}>`;
    var mailbody = `<p>Thanks for Registering</p><p>Please verify your email by clicking on the verification link below.<br/><a href=${textLink.toString()}
    >Verification Link</a></p>`
    mail(from, user.username , `Account Verification`, mailbody, function(error, success){
        callback(error, success)
    });
};

exports.sentMailForgotPassword = (user, token, callback) => {
    var textLink = "http://"+Config.server.host+":"+ Config.server.port+"/"+Config.email.resetEmailUrl+"/"+token;
    var from = `Pyrite Team<${Config.email.username}>`;
    var mailbody = `<p>Please reset your password by clicking on the link below.<br/><a href=${textLink.toString()}
    >Reset Password Link</a></p>`
    mail(from, user.username , `Account New password`, mailbody, function(error, success){
        callback(error, success)
    });
};


function decrypt(password){
    var decipher = crypto.createDecipher(algorithm, privateKey);
    var dec = decipher.update(password, 'hex', 'utf8');
    dec += decipher.final('utf8');
    return dec;
}
function encrypt(password){
    var cipher = crypto.createCipher(algorithm, privateKey);
    var crypted = cipher.update(password.toString(), 'utf8', 'hex');
    crypted += cipher.final('hex');
    return crypted;
}
function mail(from, email, subject, mailbody, callback){
    var mailOptions = {
        from: from, // sender address
        to: email, // list of receivers
        subject: subject, // Subject line
        //text: result.price, // plaintext body
        html: mailbody  // html body
    };

    smtpTransport.sendMail(mailOptions, function(error, response) {
        if (error) {
            callback(error, null)
        }
        else{
            callback(null, response)
        }
        smtpTransport.close(); // shut down the connection pool, no more messages
    });
}
