pragma solidity ^0.7.0;
// SPDX-License-Identifier: MIT

contract owned {
    address payable owner;

    modifier ownerOnly() {
        if (msg.sender != owner) revert();
        _;
    }

    constructor() {
        owner = msg.sender;
    }

    function getOwner() view public returns (address) {
        return owner;
    }
}


contract mortal is owned {
    function kill() public {
        if (msg.sender == owner) selfdestruct(owner);
    }
}


contract PriceCalculator {
    function getWarrantyPrice(/*string productId,*/ uint startDate, uint endDate, uint productPrice)  public pure returns (uint) {
        uint yrs = (endDate - startDate) / 365 days;
        /*the price is allways 5% per year*/
        return productPrice * yrs / 20;
    }
}


contract stateful {
    enum RetailerStatus {Undefined, Requested, Accepted, Rejected, Terminated}

    enum InsuranceStatus {Undefined, Requested, Active, Terminated}

    enum WarrantyStatus {Undefined, Created, Confirmed, Canceled}

    enum UserRole {Undefined, Retailer, Insurance, Owner}
}


contract related is stateful {
    struct PartnerRelations {
    RetailerStatus status;
    uint sales /*the total amount of policies sold by retailer*/;
    uint payments /*the total amount paid by retailer*/;
    uint claims /*the total amount the insurance has paid to the retailer in claims*/;
    }
}


contract InsuranceManager is owned, related {
    struct Insurance {
    string name;
    InsuranceStatus status;
    PriceCalculator priceCalculator;
    uint sales;
    uint payments;
    uint claims;
    }

    mapping (address => Insurance) insurances;

    mapping (uint => address) insuranceList;

    uint public insuranceCount;

    event InsuranceStatusChanged(
    address indexed insurance,
    InsuranceStatus status
    );

    function isInsurance(address insurance) view public returns (bool) {
        return insurances[insurance].status != InsuranceStatus.Undefined;
    }

    function createInsurance(string memory name, address priceCalculator) public {
        Insurance storage insurance = insurances[msg.sender];
        InsuranceStatus previousStatus = insurance.status;
        insurance.status = InsuranceStatus.Requested;
        if (previousStatus != InsuranceStatus.Undefined) revert();

        insurance.name = name;
        insurance.priceCalculator = PriceCalculator(priceCalculator);
        insuranceList[insuranceCount++] = msg.sender;

        emit InsuranceStatusChanged(msg.sender, InsuranceStatus.Requested);
    }

    function setInsuranceState(address insuranceAddress, InsuranceStatus status) public ownerOnly {
        Insurance  storage insurance = insurances[insuranceAddress];
        if (insurance.status == InsuranceStatus.Undefined) revert();

        insurance.status = status;
    }

    function getInsurance(uint index) view public returns (string memory name, address, InsuranceStatus) {
        Insurance  storage insurance = insurances[insuranceList[index]];
        return (insurance.name, insuranceList[index], insurance.status);
    }

    function getInsuranceByAddress(address insuranceAddress) view public returns (string memory name, address, InsuranceStatus) {
        Insurance  storage insurance = insurances[insuranceAddress];
        return (insurance.name, insuranceAddress, insurance.status);
    }

    function getInsuranceStatus(address insuranceAddress) view public returns (InsuranceStatus) {
        return insurances[insuranceAddress].status;
    }

    function getWarrantyPrice(address insuranceAddress, uint startDate, uint endDate, uint productPrice) view public returns (uint) {
        Insurance storage  insurance = insurances[insuranceAddress];
        if (insurance.status != InsuranceStatus.Active) revert();
        return insurance.priceCalculator.getWarrantyPrice(startDate, endDate, productPrice);
    }

    function getInsuranceBalance(address insuranceAddress) view public returns (uint, uint, uint) {
        Insurance  storage insurance = insurances[insuranceAddress];
        return (insurance.sales, insurance.payments, insurance.claims);
    }

    function increaseSalesBalance(address insurance, uint amount) public {
        insurances[insurance].sales += amount;
    }

    function decreaseSalesBalance(address insurance, uint amount) public {
        insurances[insurance].sales -= amount;
    }

    function increasePaymentsBalance(address insurance, uint amount) public {
        insurances[insurance].payments += amount;
    }

    function decreasePaymentsBalance(address insurance, uint amount) public {
        insurances[insurance].payments -= amount;
    }

    function increaseClaimsBalance(address insurance, uint amount) public {
        insurances[insurance].claims += amount;
    }

    function decreaseClaimsBalance(address insurance, uint amount) public {
        insurances[insurance].claims -= amount;
    }
}


contract RetailerManager is owned, related {
    InsuranceManager insuranceManager;

    constructor(address _insuranceManager) public {
        insuranceManager = InsuranceManager(_insuranceManager);
    }

    function setSubContractAddresses(address _insuranceManager) public ownerOnly {
        insuranceManager = InsuranceManager(_insuranceManager);
    }

    modifier insuranceOnly {
        if (!isInsurance(msg.sender)) revert();
        _;
    }

    function isInsurance(address insurance) view public returns (bool) {
        return insuranceManager.getInsuranceStatus(insurance) != InsuranceStatus.Undefined;
    }

    struct Retailer {
    string companyName;
    mapping (address => PartnerRelations) partnerRelations /*the mapping holds the relation of the partner with each insurance company*/;
    uint insuranceCount;
    mapping (uint => address) insurances;
    RetailerStatus status/*in order to easily check for the existence of a retailer the first status is also set on the retailer itself*/;
    uint sales;
    uint payments;
    uint claims;
    }

    mapping (address => Retailer) retailers;

    mapping (uint => address) retailerList;

    uint public retailerCount;

    event RetailerRequest(
    string indexed companyName,
    address retailerAddress,
    address indexed insurance
    );

    event RetailerStatusChanged(
    address indexed retailer,
    address indexed insurance,
    RetailerStatus status
    );

    /**
    the retailer send a transaction to request registration with an insurer
    */
    function requestRegistration(string memory companyName, address insurance) public {
        Retailer storage  retailer = retailers[msg.sender];
        retailer.companyName = companyName;
        /*make sure the insurance company exists*/
        if (insuranceManager.getInsuranceStatus(insurance) != InsuranceStatus.Active) {
            revert();
        }
        /*make sure no previous request was made*/
        if (retailer.partnerRelations[insurance].status != RetailerStatus.Undefined) {
            revert();
        }

        if (retailer.status == RetailerStatus.Undefined) {
            retailerList[retailerCount++] = msg.sender;
        }

        retailer.partnerRelations[insurance].status = RetailerStatus.Requested;
        retailer.insurances[retailer.insuranceCount++] = insurance;
        retailer.status = RetailerStatus.Accepted;
        emit RetailerRequest(companyName, msg.sender, insurance);
    }

    function getInsurance(address retailer, uint idx) view public returns (address) {
        return retailers[retailer].insurances[idx];
    }

    function getRequestState(address retailer, address insurance) view public returns (RetailerStatus) {
        return retailers[retailer].partnerRelations[insurance].status;
    }

    /**
    sets the status of a retailer's request.
    only the insurance to which the request was made can do this
    */
    function setRequestState(address retailer, RetailerStatus status) public insuranceOnly {
        retailers[retailer].partnerRelations[msg.sender].status = status;
        emit RetailerStatusChanged(retailer, msg.sender, status);
    }

    function getRetailerStatus(address retailer, address insurance) public view returns (RetailerStatus){
        return retailers[retailer].partnerRelations[insurance].status;
    }

    function getRetailerStatus(address retailer)   public view  returns (RetailerStatus){
        return retailers[retailer].status;
    }

    /**
    get the nth retailer in the list
    */
    function getRetailer(uint index, address insuranceAddress) view public returns (address, string memory, RetailerStatus, RetailerStatus) {
        address retailerAddress = retailerList[index];
        Retailer  storage retailer = retailers[retailerAddress];
        return (retailerAddress, retailer.companyName, retailer.status, retailer.partnerRelations[insuranceAddress].status);
    }

    function getRetailerByAddress(address retailerAddress, address insuranceAddress) view public returns (address, string memory, RetailerStatus, RetailerStatus) {
        Retailer  storage retailer = retailers[retailerAddress];
        return (retailerAddress, retailer.companyName, retailer.status, retailer.partnerRelations[insuranceAddress].status);
    }

    function getRetailerBalances(address retailer, address insurance) view public returns (uint, uint, uint) {
        PartnerRelations  storage  partnerRelation = retailers[retailer].partnerRelations[insurance];
        return (partnerRelation.sales, partnerRelation.payments, partnerRelation.claims);
    }

    function getRetailerTotalBalances(address retailerAddress) view public returns (uint, uint, uint) {
        Retailer storage  retailer = retailers[retailerAddress];
        return (retailer.sales, retailer.payments, retailer.claims);
    }

    function increaseSalesBalance(address retailer, address insurance, uint price) public {
        retailers[retailer].partnerRelations[insurance].sales += price;
        retailers[retailer].sales += price;
    }

    function decreaseSalesBalance(address retailer, address insurance, uint price) public {
        retailers[retailer].partnerRelations[insurance].sales -= price;
        retailers[retailer].sales -= price;
    }

    function increasePaymentsBalance(address retailer, address insurance, uint amount) public {
        retailers[retailer].partnerRelations[insurance].payments += amount;
        retailers[retailer].payments += amount;
    }

    function decreasePaymentsBalance(address retailer, address insurance, uint amount) public {
        retailers[retailer].partnerRelations[insurance].payments -= amount;
        retailers[retailer].payments -= amount;
    }

    function increaseClaimsBalance(address retailer, address insurance, uint amount) public {
        retailers[retailer].partnerRelations[insurance].claims += amount;
        retailers[retailer].claims += amount;
    }

    function decreaseClaimsBalance(address retailer, address insurance, uint amount) public {
        retailers[retailer].partnerRelations[insurance].claims -= amount;
        retailers[retailer].claims -= amount;
    }
}


contract Insurechain is mortal, stateful {
    InsuranceManager insuranceManager;

    RetailerManager retailerManager;

    constructor(address _insuranceManager, address _retailerManager) public {
        insuranceManager = InsuranceManager(_insuranceManager);
        retailerManager = RetailerManager(_retailerManager);
    }

    function setSubContractAddresses(address _insuranceManager, address _retailerManager) public ownerOnly {
        insuranceManager = InsuranceManager(_insuranceManager);
        retailerManager = RetailerManager(_retailerManager);
    }

    struct Claim {
    address retailer /*in theory another retailer than the one who sold the insurance can make a claim*/;
    uint amount;
    string description;
    }

    struct Warranty {
    address retailer;
    uint startDate;
    uint endDate;
    string policyNumber;
    WarrantyStatus status;
    uint productPrice;
    uint warrantyPrice;
    mapping (uint => Claim) claims;
    uint claimCount;
    }

    // mapping of insurance -> productId -> serialNumber -> Warranty
    mapping (address => mapping (string => mapping (string => uint))) warranties;

    uint public warrantyCount;

    mapping (uint => Warranty) warrantyList;

    modifier insuranceOnly {
        if (!isInsurance(msg.sender)) revert();
        _;
    }

    function isInsurance(address insurance) view public returns (bool) {
        return insuranceManager.getInsuranceStatus(insurance) != InsuranceStatus.Undefined;
    }

    modifier registeredRetailerOnly(address insurance)  {
        if (!isRegisteredRetailer(insurance, msg.sender)) revert();
        _;
    }

    function isRegisteredRetailer(address insurance, address retailer) view public returns (bool) {
        return isInsurance(insurance) && retailerManager.getRequestState(retailer, insurance) == RetailerStatus.Accepted;
    }

    function getRole(address user) view public returns (UserRole) {
        if (user == owner) return UserRole.Owner;
        if (retailerManager.getRetailerStatus(user) == RetailerStatus.Accepted) return UserRole.Retailer;
        if (insuranceManager.getInsuranceStatus(user) == InsuranceStatus.Active) return UserRole.Insurance;

        return UserRole.Undefined;
    }

    function getWarrantyQuote(/*string productId, */address insurance, uint startDate, uint endDate, uint productPrice)
    public view returns (uint warrantyPrice) {
        return insuranceManager.getWarrantyPrice(insurance, startDate, endDate, productPrice);
    }

    /**
        Creates a new warranty.
        productId: The EAN13 that identifies the product
        serialNumber: the particular product serial number
        insurance: the eth address of the insurance
        startDate: start date of the extended warranty
        endDate: start date of the extended warranty
        price: the price in cents
    */
    function createWarranty(string memory productId, string memory serialNumber, address insurance, uint startDate, uint endDate, uint productPrice) public registeredRetailerOnly(insurance) {
        uint idx = warranties[insurance][productId][serialNumber];
        Warranty  storage warranty = warrantyList[idx];
        if (warranty.status != WarrantyStatus.Undefined) revert();

        warranty.status = WarrantyStatus.Created;
        warranty.startDate = startDate;
        warranty.endDate = endDate;
        warranty.productPrice = productPrice;
        warranty.retailer = msg.sender;
        warranty.warrantyPrice = insuranceManager.getWarrantyPrice(insurance, startDate, endDate, productPrice);
        warrantyList[++warrantyCount] = warranty;
        warranties[insurance][productId][serialNumber] = warrantyCount;
        retailerManager.increaseSalesBalance(msg.sender, insurance, warranty.warrantyPrice);
        insuranceManager.increaseSalesBalance(insurance, warranty.warrantyPrice);
    }

    /**
        Confirms a warranty
        productId: The EAN13 that identifies the product
        serialNumber: the particular product serial number
        policyNumber: the policy number of the warranty
    */
    function confirmWarranty(string memory productId, string memory serialNumber, string memory policyNumber) public insuranceOnly {
        uint idx = warranties[msg.sender][productId][serialNumber];
        Warranty  storage warranty = warrantyList[idx];
        if (warranty.status != WarrantyStatus.Created) revert();

        warranty.status = WarrantyStatus.Confirmed;
        warranty.policyNumber = policyNumber;
    }

    /**
        Cacnels a warranty
        productId: The EAN13 that identifies the product
        serialNumber: the particular product serial number
        policyNumber: the policy number of the warranty
    */
    function cancelWarranty(string memory productId, string memory serialNumber, address insuranceAddress) public registeredRetailerOnly(insuranceAddress) {
        uint idx = warranties[insuranceAddress][productId][serialNumber];
        Warranty  storage warranty = warrantyList[idx];
        /*a warranty can only be canceled if it exists and no claims have been made*/
        if (warranty.status == WarrantyStatus.Undefined || warranty.claimCount > 0) revert();

        warranty.status = WarrantyStatus.Canceled;
        retailerManager.decreaseSalesBalance(msg.sender, insuranceAddress, warranty.warrantyPrice);
        insuranceManager.decreaseSalesBalance(insuranceAddress, warranty.warrantyPrice);
    }

    function getWarranty(string memory productId, string memory serialNumber, address insurance) view public returns (uint startDate, uint endDate, WarrantyStatus status,
    string memory policyNumber, uint warrantyPrice, uint claimCount) {
        return getWarrantyByIndex(warranties[insurance][productId][serialNumber]);
    }

    function getWarrantyByIndex(uint idx) view public returns (uint startDate, uint endDate, WarrantyStatus status,
    string memory policyNumber, uint warrantyPrice, uint claimCount) {
        Warranty storage  warranty = warrantyList[idx];
        return (warranty.startDate, warranty.endDate, warranty.status, warranty.policyNumber, warranty.warrantyPrice, warranty.claimCount);
    }

    function isWarrantyValid(address insurance, string memory productId, string memory serialNumber) view public returns (bool) {
        uint idx = warranties[insurance][productId][serialNumber];
        /*the index can not be zero based because else the first warranty would be the default but I don't want to make it 1 based for the user*/
        Warranty storage  warranty = warrantyList[idx];
        return warranty.status == WarrantyStatus.Confirmed && warranty.startDate < now && warranty.endDate > now;
    }

    /**
        create a new claim for an insured product
        productId: The EAN13 that identifies the product
        serialNumber: the particular product serial number
    */
    function createClaim(string memory productId, string memory serialNumber, address insurance, uint amount, string memory description) public registeredRetailerOnly(insurance) {
        /*create only works for existing and valid warranties*/
        if (!isWarrantyValid(insurance, productId, serialNumber)) revert();

        uint idx = warranties[insurance][productId][serialNumber];
        Warranty  storage warranty = warrantyList[idx];

        Claim  storage claim = warranty.claims[warranty.claimCount++];
        claim.retailer = msg.sender;
        claim.amount = amount;
        claim.description = description;

        /*increase the retailer's account*/
        retailerManager.increaseClaimsBalance(msg.sender, insurance, amount);
        insuranceManager.increaseClaimsBalance(insurance, amount);
    }

    function getClaim(string memory productId, string memory serialNumber, address insurance, uint idx) view public returns (address retailer, uint amount, string memory description) {
        uint wIdx = warranties[insurance][productId][serialNumber];
        Claim  storage claim = warrantyList[idx].claims[wIdx];
        return (claim.retailer, claim.amount, claim.description);
    }
}
