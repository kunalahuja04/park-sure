import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ParkSureService } from './services/park-sure.service';
import { PolicyEntity } from './models/policy.model';
import QRCode from 'qrcode';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  parkSure = inject(ParkSureService);
  activeTab = signal<'scanner' | 'insure' | 'passes' | 'stations'>('insure');

  selectedStationId = signal<string>('st_and');
  vehicleNumber = signal<string>('MH02EK9842');
  vehicleType = signal<'MOTORCYCLE' | 'SCOOTER' | 'ELECTRIC_EV'>('SCOOTER');
  ownerName = signal<string>('Kunal Ahuja');
  ownerPhone = signal<string>('+91 98200 12345');
  selectedPlanId = signal<string>('basic_commuter');
  
  selectedPass = signal<PolicyEntity | null>(null);
  qrDataUrl = signal<string>('');
  showClaimModal = signal<boolean>(false);
  claimIncident = signal<string>('Minor Scratches / Mirror Broken');
  claimNotes = signal<string>('');
  claimAmount = signal<number>(1500);

  scannedCode = signal<string | null>(null);

  setTab(tab: 'scanner' | 'insure' | 'passes' | 'stations') {
    this.activeTab.set(tab);
  }

  handleActivate() {
    if (!this.vehicleNumber()) return;
    const policy = this.parkSure.activatePolicy({
      stationId: this.selectedStationId(),
      vehicleNumber: this.vehicleNumber(),
      vehicleType: this.vehicleType(),
      ownerName: this.ownerName(),
      ownerPhone: this.ownerPhone(),
      planId: this.selectedPlanId()
    });
    this.openPassModal(policy);
    this.setTab('passes');
  }

  async openPassModal(policy: PolicyEntity) {
    this.selectedPass.set(policy);
    try {
      const url = await QRCode.toDataURL(policy.qrCodeData, {
        width: 300,
        margin: 2,
        color: { dark: '#191C19', light: '#FFFFFF' }
      });
      this.qrDataUrl.set(url);
    } catch (e) {
      console.error('QR generation error', e);
    }
  }

  closePassModal() {
    this.selectedPass.set(null);
  }

  openClaim(policy: PolicyEntity) {
    this.selectedPass.set(policy);
    this.showClaimModal.set(true);
  }

  submitClaim() {
    const policy = this.selectedPass();
    if (!policy) return;
    this.parkSure.fileClaim(
      policy.id,
      this.claimIncident(),
      this.claimNotes(),
      this.claimAmount()
    );
    this.showClaimModal.set(false);
    this.closePassModal();
    alert('✅ Claim filed successfully! ParkSure underwriting team is reviewing your baseline timestamped proof.');
  }

  simulateScan(code: string) {
    this.scannedCode.set(code);
  }
}
