import { Injectable, signal, computed } from '@angular/core';
import { Station, PlanTier, PolicyEntity, ClaimRecord } from '../models/policy.model';

@Injectable({
  providedIn: 'root'
})
export class ParkSureService {
  private readonly POLICIES_KEY = 'parksure_policies_v1';
  private readonly CLAIMS_KEY = 'parksure_claims_v1';

  readonly plans = signal<PlanTier[]>([
    {
      id: 'basic_commuter',
      name: 'Commuter Basic',
      dailyPrice: 5,
      maxCoverage: 25000,
      theftCovered: true,
      accidentalDamage: true,
      partsTheft: false,
      isPopular: true
    },
    {
      id: 'pro_shield',
      name: 'Shield Pro',
      dailyPrice: 10,
      maxCoverage: 50000,
      theftCovered: true,
      accidentalDamage: true,
      partsTheft: true
    }
  ]);

  readonly stations = signal<Station[]>([
    { id: 'st_dad', name: 'Dadar Junction', code: 'DDR', line: 'WESTERN', cctvCovered: true, guardOnDuty: true, riskLevel: 'LOW', totalSpots: 450, availableSpots: 82 },
    { id: 'st_and', name: 'Andheri West Metro Gate', code: 'ADH', line: 'WESTERN', cctvCovered: true, guardOnDuty: true, riskLevel: 'LOW', totalSpots: 600, availableSpots: 140 },
    { id: 'st_cst', name: 'CSMT Mumbai Terminus', code: 'CSMT', line: 'CENTRAL', cctvCovered: true, guardOnDuty: true, riskLevel: 'LOW', totalSpots: 520, availableSpots: 110 },
    { id: 'st_bor', name: 'Borivali East', code: 'BVI', line: 'WESTERN', cctvCovered: true, guardOnDuty: true, riskLevel: 'LOW', totalSpots: 750, availableSpots: 195 },
    { id: 'st_tha', name: 'Thane West Platform 1', code: 'TNA', line: 'CENTRAL', cctvCovered: true, guardOnDuty: true, riskLevel: 'LOW', totalSpots: 620, availableSpots: 74 },
    { id: 'st_ban', name: 'Bandra Station West', code: 'BA', line: 'WESTERN', cctvCovered: true, guardOnDuty: true, riskLevel: 'LOW', totalSpots: 380, availableSpots: 55 },
    { id: 'st_kur', name: 'Kurla Junction', code: 'CLA', line: 'CENTRAL', cctvCovered: true, guardOnDuty: false, riskLevel: 'MEDIUM', totalSpots: 400, availableSpots: 35 },
    { id: 'st_gkp', name: 'Ghatkopar East', code: 'GC', line: 'CENTRAL', cctvCovered: true, guardOnDuty: true, riskLevel: 'LOW', totalSpots: 490, availableSpots: 92 },
    { id: 'st_kyn', name: 'Kalyan Junction', code: 'KYN', line: 'CENTRAL', cctvCovered: true, guardOnDuty: true, riskLevel: 'LOW', totalSpots: 550, availableSpots: 120 },
    { id: 'st_vsh', name: 'Vashi Station Plaza', code: 'VSH', line: 'HARBOUR', cctvCovered: true, guardOnDuty: true, riskLevel: 'LOW', totalSpots: 510, availableSpots: 160 }
  ]);

  readonly policies = signal<PolicyEntity[]>(this.loadPolicies());
  readonly claims = signal<ClaimRecord[]>(this.loadClaims());

  readonly activePolicies = computed(() => {
    const now = Date.now();
    return this.policies().filter(p => p.status === 'ACTIVE' && p.endTime > now);
  });

  constructor() {
    if (this.policies().length === 0) {
      this.activatePolicy({
        stationId: 'st_and',
        vehicleNumber: 'MH02EK9842',
        vehicleType: 'SCOOTER',
        ownerName: 'Kunal Ahuja',
        ownerPhone: '+91 98200 12345',
        planId: 'basic_commuter'
      });
    }
  }

  activatePolicy(data: {
    stationId: string;
    vehicleNumber: string;
    vehicleType: 'MOTORCYCLE' | 'SCOOTER' | 'ELECTRIC_EV';
    ownerName: string;
    ownerPhone: string;
    planId: string;
  }): PolicyEntity {
    const station = this.stations().find(s => s.id === data.stationId) || this.stations()[0];
    const plan = this.plans().find(p => p.id === data.planId) || this.plans()[0];
    const now = Date.now();
    const policyNumber = `PS-MUM-${Math.floor(100000 + Math.random() * 900000)}`;

    const newPolicy: PolicyEntity = {
      id: 'pol_' + now,
      policyNumber,
      stationId: station.id,
      stationName: station.name,
      vehicleNumber: data.vehicleNumber.toUpperCase().trim(),
      vehicleType: data.vehicleType,
      ownerName: data.ownerName.trim(),
      ownerPhone: data.ownerPhone.trim(),
      planId: plan.id,
      planName: plan.name,
      premiumPaid: plan.dailyPrice,
      coverageAmount: plan.maxCoverage,
      startTime: now,
      endTime: now + (24 * 60 * 60 * 1000),
      status: 'ACTIVE',
      qrCodeData: `PARKSURE:${policyNumber}:${data.vehicleNumber}:${station.code}:${now + 86400000}`
    };

    const updated = [newPolicy, ...this.policies()];
    this.policies.set(updated);
    this.savePolicies(updated);
    return newPolicy;
  }

  fileClaim(policyId: string, incidentType: string, notes: string, amount: number) {
    const policy = this.policies().find(p => p.id === policyId);
    if (!policy) return;

    const newClaim: ClaimRecord = {
      id: 'clm_' + Date.now(),
      policyId,
      policyNumber: policy.policyNumber,
      stationName: policy.stationName,
      vehicleNumber: policy.vehicleNumber,
      incidentType,
      notes,
      estimatedAmount: amount,
      status: 'UNDER_REVIEW',
      timestamp: Date.now()
    };

    const updatedClaims = [newClaim, ...this.claims()];
    this.claims.set(updatedClaims);
    localStorage.setItem(this.CLAIMS_KEY, JSON.stringify(updatedClaims));
  }

  private loadPolicies(): PolicyEntity[] {
    try {
      const data = localStorage.getItem(this.POLICIES_KEY);
      return data ? JSON.parse(data) : [];
    } catch {
      return [];
    }
  }

  private savePolicies(list: PolicyEntity[]) {
    localStorage.setItem(this.POLICIES_KEY, JSON.stringify(list));
  }

  private loadClaims(): ClaimRecord[] {
    try {
      const data = localStorage.getItem(this.CLAIMS_KEY);
      return data ? JSON.parse(data) : [];
    } catch {
      return [];
    }
  }
}
