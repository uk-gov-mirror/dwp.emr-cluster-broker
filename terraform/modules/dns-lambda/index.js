const AWS = require('aws-sdk');
const emr = new AWS.EMR();
const route53 = new AWS.Route53();

exports.handler = (event, context) => {

    let action;
    //checking the EMR state and set the action of CName
    if (JSON.stringify(event.detail.state) == "\"RUNNING\"") {
        action = "CREATE";
    } else {
        action = "DELETE";
    }

    let clusterId = event.detail.clusterId;
    clusterId = clusterId.replace(/"([^"]+(?="))"/g, '$1');
    let dnsName;
    let hostedZoneId;
    // grabs cluster ID from response JSON
    let params = {
        //  sets up ClusterID to identify emr cluster
        ClusterId: clusterId
    };

    emr.describeCluster(params, function (err, data) {
        if (err) console.log(err, err.stack);                                                 // an error occurred
        else {
            let emrJson = data.Cluster;             // TO BE CHECKED IF NEEDED .Reservations[0].Instances[0];                               // successful response
            // grabs domain name of master node in emr
            dnsName = emrJson.MasterPublicDnsName;
            // finds hosted zone ID - passed into emr cluster as a tag by cluster broker
            emrJson.Tags.forEach(e => {
                if (e.Key == "hosted_zone_id") {
                    // setting Hosted zone id from tags
                    hostedZoneId = e.Value;
                } else {
                    console.log(`No Hosted Zone ID found for cluster ID : ${clusterId}`);
                    return;
                }
            });

        }

        params = {
            ChangeBatch: {
                Changes: [
                    {
                        Action: action,
                        ResourceRecordSet: {

                            Name: `${clusterId}.dnsalias.local`,
                            ResourceRecords: [
                                {
                                    Value: dnsName
                                }
                            ],
                            TTL: 60,
                            Type: "CNAME"
                        }

                    }
                ],
            },
            HostedZoneId: hostedZoneId
        };

        //  sets new CNAME
        route53.changeResourceRecordSets(params, function (err, data) {
            if (err) {
                console.log(err, err.stack); // an error occurred
            }
            else {
                console.log(data);           // successful response
            }

        });

    });
};
