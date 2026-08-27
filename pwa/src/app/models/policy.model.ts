export type LineType = 'WESTERN' | 'CENTRAL' | 'HARBOUR';
export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH';
export type VehicleType = 'MOTORCYCLE' | 'SCOOTER' | 'ELECTRIC_EV';

export interface Station {
  id: string;
  name: string;
  code: string;
  line: LineType;
  cctvCovered: boolean;
  guardOnDuty: boolean;
  riskLevel: RiskLevel;
  totalSpots: number;
  availableSpots: number;
}

export interface PlanTier {
  id: string;
  name: string;
  dailyPrice: number;
  maxCoverage: number;
  theftCovered: boolean;
  accidentalDamage: boolean;
  partsTheft: boolean;
  isPopular?: boolean;
}

export interface PolicyEntity {
  id: string;
  policyNumber: string;
  stationId: string;
  stationName: string;
  vehicleNumber: string;
  vehicleType: VehicleType;
  ownerName: string;
  ownerPhone: string;
  planId: string;
  planName: string;
  premiumPaid: number;
  coverageAmount: number;
  startTime: number;
  endTime: number;
  status: 'ACTIVE' | 'EXPIRED' | 'CLAIMED';
  qrCodeData: string;
}

export interface ClaimRecord {
  id: string;
  policyId: string;
  policyNumber: string;
  stationName: string;
  vehicleNumber: string;
  incidentType: string;
  notes: string;
  estimatedAmount: number;
  status: 'SUBMITTED' | 'UNDER_REVIEW' | 'APPROVED' | 'DISBURSED';
  timestamp: number;
}
