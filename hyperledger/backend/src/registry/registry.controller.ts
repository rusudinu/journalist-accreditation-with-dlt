import {Body, Controller, Get, Post} from '@nestjs/common';
import {RegistryService} from './registry.service';
import {RegistryModel} from './registry.model';

@Controller('registry')
export class RegistryController {
    constructor(private readonly registryService: RegistryService) {

    }


    @Get()
    getRegistry(): string {
        return 'hello world';
    }

    @Get(':requestId')
    getRegistryByRequestId(): RegistryModel {
        return {
            RequestID: '1',
            RequestSnapshotHash: 'hash1',
        };
    }

    @Post()
    saveOrUpdateRegistry(@Body() request: RegistryModel): void {
        // this.registryService.saveOrUpdateRegistry(request);
        console.log(request);
    }
}
