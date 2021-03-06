import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-board',
  templateUrl: './board.component.html',
  styleUrls: ['./board.component.css']
})
export class BoardComponent  {
  @Input() pieces : any;
  @Output() onSquareClicked : EventEmitter<any> = new EventEmitter<any>();

  constructor() {
  }

  onSquareClick(i, j) {
    this.onSquareClicked.emit({
      i: i,
      j: j
    });
  }
}
